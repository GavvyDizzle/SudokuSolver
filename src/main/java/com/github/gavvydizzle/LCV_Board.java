package com.github.gavvydizzle;

import java.math.BigInteger;

public record LCV_Board(Board board, BigInteger permutations) implements Comparable<LCV_Board> {
    @Override
    public int compareTo(LCV_Board o) {
        return o.permutations.compareTo(permutations);
    }
}
