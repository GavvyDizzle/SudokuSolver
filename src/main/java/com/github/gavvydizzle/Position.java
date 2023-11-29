package com.github.gavvydizzle;

import java.util.ArrayList;
import java.util.Collection;

public class Position {

    private int answer;
    private final ArrayList<Integer> possibleAnswers;

    public Position() {
        this.answer = 0;
        possibleAnswers = new ArrayList<>(9);
        for (int i = 1; i <= 9; i++) {
            possibleAnswers.add(i);
        }
    }

    public Position(int answer) {
        this.answer = answer;
        possibleAnswers = new ArrayList<>();
    }

    /**
     * @return A clone of this position
     */
    public Position clonePosition() {
        Position np = new Position(answer);
        np.possibleAnswers.addAll(this.possibleAnswers);
        return np;
    }

    public void removePossibleAnswers(Collection<Integer> answers) {
        possibleAnswers.removeAll(answers);
    }

    public void removePossibleAnswer(int answer) {
        possibleAnswers.remove(Integer.valueOf(answer));
    }

    /**
     * Sets the answer for this position and empties the possible answer list
     * @param answer The answer
     */
    public void solve(int answer) {
        this.answer = answer;
        possibleAnswers.clear();
    }

    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }

    public ArrayList<Integer> getPossibleAnswers() {
        return possibleAnswers;
    }

    public int getNumPossibleAnswers() {
        return possibleAnswers.size();
    }
}
