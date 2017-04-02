package org.nibiru.j2cs;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

public class DescIterable implements Iterable<String> {
    private final String data;

    public DescIterable(String data) {
        this.data = checkNotNull(data);
    }

    @Override
    public Iterator<String> iterator() {
        return new DescIterator(data);
    }

    private static class DescIterator implements Iterator<String> {
        private final String data;
        private int position;

        private DescIterator(String data) {
            this.data = data;
        }

        @Override
        public boolean hasNext() {
            return position < data.length();
        }

        @Override
        public String next() {
            int end = position;
            while (data.charAt(end) == '[') {
                end++;
            }
            if (data.charAt(end) == 'L') {
                while (data.charAt(end) != ';') {
                    end++;
                }
            }
            end++;
            String value = data.substring(position, end);
            position = end;
            return value;
        }
    }
}
