package it.gov.pagopa.paymentupdater.util;

import java.util.List;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class FunctionalUtils {
	
	private FunctionalUtils() {
		
	}

    /**
     * Safely gets the first element of a {@link List}
     * 
     * @param list a {@code List}
     * @param <E>  type of elements in list
     * @return the first element of list or {@link Optional#absent()} if absent or
     *         null
     */
    @NonNull
    public static <E> Optional<E> firstElement(@Nullable List<E> list) {
        if (list == null || list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(list.get(0));

    }
}
