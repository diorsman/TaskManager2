package com.personal.taskmanager2.utilities;

public class IconKey {
    char character;
    int color;

    public IconKey(char character, int color) {

        this.character = character;
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IconKey iconKey = (IconKey) o;

        if (character != iconKey.character) {
            return false;
        }
        if (color != iconKey.color) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {

        int result = (int) character;
        result = 31 * result + color;
        return result;
    }
}
