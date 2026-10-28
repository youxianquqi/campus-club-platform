/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.endtiem.admin.entity.User;
import com.endtiem.admin.mapper.UserMapper;
import com.endtiem.club.entity.Club;
import com.endtiem.club.mapper.ClubMapper;
import com.endtiem.common.util.JwtUtil;
import com.endtiem.platform.entity.ChatMessage;
import com.endtiem.platform.mapper.ChatMessageMapper;
import com.endtiem.platform.util.ClubPairChannel;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatMessageMapper messageMapper;
    private final UserMapper userMapper;
    private final ClubMapper clubMapper;
    private final JwtUtil jwtUtil;

    public ChatController(
            ChatMessageMapper messageMapper,
            UserMapper userMapper,
            ClubMapper clubMapper,
            JwtUtil jwtUtil) {
        this.messageMapper = messageMapper;
        this.userMapper = userMapper;
        this.clubMapper = clubMapper;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/context")
    public Map<String, Object> clubChatContext() {
        Integer myClubId = resolveManagedClubId();
        Map<String, Object> data = new HashMap<>();
        if (myClubId == null) {
            data.put("leader", false);
            data.put("clubId", null);
            data.put("clubName", null);
        } else {
            Club c = clubMapper.selectById(myClubId);
            data.put("leader", true);
            data.put("clubId", myClubId);
            data.put("clubName", c != null ? c.getName() : null);
        }
        Map<String, Object> body = new HashMap<>();
        body.put("code", 200);
        body.put("data", data);
        return body;
    }

    @GetMapping("/threads")
    public Map<String, Object> listClubThreads() {
        Integer myClubId = resolveManagedClubId();
        if (myClubId == null) {
            throw new RuntimeException("仅社团负责人可使用社团间联系");
        }
        List<Long> keys = messageMapper.selectDistinctClubChannelKeys();
        List<Map<String, Object>> threads = new ArrayList<>();
        for (Long channelKey : keys) {
            if (channelKey == null || channelKey == 0L) {
                continue;
            }
            int peerId = ClubPairChannel.peerClubId(channelKey, myClubId);
            if (peerId <= 0) {
                continue;
            }
            Page<ChatMessage> lastPage =
                    messageMapper.pageByClubChannelOrderByCreatedAtDesc(
                            channelKey, new Page<>(1, 1));
            if (lastPage.getRecords().isEmpty()) {
                continue;
            }
            ChatMessage last = lastPage.getRecords().get(0);
            Club peer = clubMapper.selectById(peerId);
            Map<String, Object> row = new HashMap<>();
            row.put("channelKey", channelKey);
            row.put("peerClubId", peerId);
            row.put("peerClubName", peer != null ? peer.getName() : ("社团 #" + peerId));
            row.put("lastMessage", last.getContent());
            row.put("lastAt", last.getCreatedAt());
            threads.add(row);
        }
        threads.sort(
                (a, b) -> {
                    LocalDateTime ta = (LocalDateTime) a.get("lastAt");
                    LocalDateTime tb = (LocalDateTime) b.get("lastAt");
                    if (ta == null && tb == null) {
                        return 0;
                    }
                    if (ta == null) {
                        return 1;
                    }
                    if (tb == null) {
                        return -1;
                    }
                    return tb.compareTo(ta);
                });
        Map<String, Object> body = new HashMap<>();
        body.put("code", 200);
        body.put("data", threads);
        return body;
    }

    @GetMapping("/messages")
    public Map<String, Object> getMessages(
            @RequestParam(required = false) Integer peerClubId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        if (peerClubId == null || peerClubId <= 0) {
            throw new RuntimeException("请指定对方社团 ID（peerClubId）");
        }
        Integer myClubId = resolveManagedClubId();
        if (myClubId == null) {
            throw new RuntimeException("仅社团负责人可查看社团间消息");
        }
        if (peerClubId.equals(myClubId)) {
            throw new RuntimeException("不能与自己所在社团建立联系频道");
        }
        Club myClub = clubMapper.selectById(myClubId);
        Club peerClub = clubMapper.selectById(peerClubId);
        if (peerClub == null) {
            throw new RuntimeException("对方社团不存在");
        }
        long channel = ClubPairChannel.encode(myClubId, peerClubId);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int p = Math.max(page, 0);
        Page<ChatMessage> messages =
                messageMapper.pageByClubChannelOrderByCreatedAtDesc(
                        channel, new Page<>(p + 1L, safeSize));

        Integer currentUserId = getCurrentUserId();
        List<Map<String, Object>> messageList = new ArrayList<>();
        for (ChatMessage msg : messages.getRecords()) {
            User sender = userMapper.selectById(msg.getSenderId());
            Map<String, Object> msgInfo = new HashMap<>();
            msgInfo.put("id", msg.getId());
            msgInfo.put("senderId", msg.getSenderId());
            msgInfo.put("senderName", sender != null ? sender.getRealName() : "");
            msgInfo.put("senderAvatar", sender != null ? sender.getAvatar() : null);
            msgInfo.put("content", msg.getContent());
            msgInfo.put("createdAt", msg.getCreatedAt());
            msgInfo.put("isMine", msg.getSenderId().equals(currentUserId));
            Integer sc = resolveSenderClubId(msg.getSenderId(), myClub, peerClub);
            msgInfo.put("senderClubId", sc);
            msgInfo.put("senderClubName", resolveSenderClubName(sc, myClub, peerClub));
            messageList.add(msgInfo);
        }
        Collections.reverse(messageList);

        Map<String, Object> result = new HashMap<>();
        result.put("messages", messageList);
        result.put("total", messages.getTotal());
        result.put("pages", messages.getPages());
        return result;
    }

    @PostMapping("/messages")
    public Map<String, Object> sendMessage(@RequestBody Map<String, Object> request) {
        Integer currentUserId = getCurrentUserId();
        Integer myClubId = resolveManagedClubId();
        if (myClubId == null) {
            throw new RuntimeException("仅社团负责人可发送社团间消息");
        }
        Integer peerClubId = toInteger(request.get("peerClubId"));
        String content =
                request.get("content") != null ? String.valueOf(request.get("content")) : null;

        Map<String, Object> response = new HashMap<>();
        if (peerClubId == null || peerClubId <= 0) {
            response.put("success", false);
            response.put("message", "请指定对方社团 ID（peerClubId）");
            return response;
        }
        if (peerClubId.equals(myClubId)) {
            response.put("success", false);
            response.put("message", "不能向本社团发送频道消息");
            return response;
        }
        if (content == null || content.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "消息内容不能为空");
            return response;
        }
        Club peerClub = clubMapper.selectById(peerClubId);
        if (peerClub == null) {
            response.put("success", false);
            response.put("message", "对方社团不存在");
            return response;
        }

        long channel = ClubPairChannel.encode(myClubId, peerClubId);
        LocalDateTime now = LocalDateTime.now();
        ChatMessage message = new ChatMessage();
        message.setSenderId(currentUserId);
        message.setContent(content.trim());
        message.setCreatedAt(now);
        message.setReceiverId(channel);
        message.setSentAt(now);
        messageMapper.insert(message);

        response.put("success", true);
        response.put("message", "发送成功");
        response.put("messageId", message.getId());
        response.put("createdAt", message.getCreatedAt());
        return response;
    }

    private static Integer toInteger(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Integer) {
            return (Integer) o;
        }
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        try {
            return Integer.parseInt(o.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer resolveSenderClubId(Integer senderUserId, Club my, Club peer) {
        if (senderUserId == null || my == null || peer == null) {
            return null;
        }
        if (my.getAdminId() != null && my.getAdminId().equals(senderUserId)) {
            return my.getId();
        }
        if (peer.getAdminId() != null && peer.getAdminId().equals(senderUserId)) {
            return peer.getId();
        }
        return null;
    }

    private static String resolveSenderClubName(Integer senderClubId, Club my, Club peer) {
        if (senderClubId == null) {
            return "";
        }
        if (my != null && senderClubId.equals(my.getId())) {
            return my.getName() != null ? my.getName() : "";
        }
        if (peer != null && senderClubId.equals(peer.getId())) {
            return peer.getName() != null ? peer.getName() : "";
        }
        return "";
    }

    private Integer getCurrentUserId() {
        String token = bearerToken();
        return jwtUtil.getRelatedIdFromToken(token);
    }

    private Integer resolveManagedClubId() {
        String token = bearerToken();
        Integer role = jwtUtil.getRoleFromToken(token);
        if (role == null || role < 2) {
            return null;
        }
        Integer userId = jwtUtil.getRelatedIdFromToken(token);
        if (userId == null) {
            return null;
        }
        List<Club> allClubs = clubMapper.selectList(null);
        for (Club club : allClubs) {
            if (club.getAdminId() != null && club.getAdminId().equals(userId)) {
                return club.getId();
            }
        }
        return null;
    }

    private static String bearerToken() {
        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String auth = httpRequest.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new RuntimeException("未授权访问");
        }
        return auth.substring(7);
    }
}
