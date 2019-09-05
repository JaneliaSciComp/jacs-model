package org.janelia.rendering;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Streamable<T> {
    private static final Streamable<?> EMPTY = new Streamable<>(null, 0);

    @SuppressWarnings("unchecked")
    public static <U> Streamable<U> empty() {
        return (Streamable<U>) EMPTY;
    }

    public static <U> Streamable<U> of(U content, long size) {
        return new Streamable<U>(content, size);
    }

    private long size;
    private T content;

    private Streamable(T content, long size) {
        this.size = size;
        this.content = content;
    }

    public long getSize() {
        return size;
    }

    public T getContent() {
        return content;
    }

    public Optional<T> asOptional() {
        if (content == null) {
            return Optional.empty();
        } else {
            return Optional.of(content);
        }
    }

    /**
     * This method consumes the enclosed content.
     *
     * @param contentMapper
     * @param sizeMapper
     * @param <U>
     * @return
     */
    public <U> Streamable<U> consume(Function<? super T, ? extends U> contentMapper,
                                     BiFunction<? super U, Long, Long> sizeMapper) {
        Objects.requireNonNull(contentMapper);
        Objects.requireNonNull(sizeMapper);
        if (content != null) {
            // this is not quite right but we only map the content and leave the size the same
            try {
                U newContent = contentMapper.apply(content);
                if (newContent == null) {
                    return empty();
                } else {
                    return Streamable.of(newContent, sizeMapper.apply(newContent, size));
                }
            } finally {
                content = null;
                size = 0;
            }
        } else {
            return empty();
        }
    }

}
