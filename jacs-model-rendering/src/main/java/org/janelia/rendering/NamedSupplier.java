package org.janelia.rendering;

import java.util.function.Supplier;

public interface NamedSupplier<T> extends Supplier<T> {

    static <U> NamedSupplier<U> namedSupplier(String name, Supplier<U> supplier) {
        return new NamedSupplier<U>() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public U get() {
                return supplier.get();
            }
        };
    }

    String getName();
}
