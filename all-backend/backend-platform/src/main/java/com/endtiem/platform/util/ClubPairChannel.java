/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.util;

/** 将两个社团 ID 编码为 chat_message.receiver_id（&gt;0），用于社团间联系频道。 与公共聊天室 receiver_id=0 区分。 */
public final class ClubPairChannel {

    private ClubPairChannel() {}

    public static long encode(int clubIdA, int clubIdB) {
        int lo = Math.min(clubIdA, clubIdB);
        int hi = Math.max(clubIdA, clubIdB);
        return ((long) lo << 32) | (hi & 0xffffffffL);
    }

    /**
     * @return 对方社团 id；若 channel 不包含 myClubId 则返回 -1
     */
    public static int peerClubId(long channel, int myClubId) {
        int lo = (int) (channel >>> 32);
        int hi = (int) (channel & 0xffffffffL);
        if (myClubId == lo) {
            return hi;
        }
        if (myClubId == hi) {
            return lo;
        }
        return -1;
    }
}
