package org.janelia.model.domain.gui.cdmip;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;

import org.janelia.model.domain.Reference;

public class ColorDepthLibraryUtils {

    private static final Set<String> NON_SEARCHABLE_VARIANTS = ImmutableSet.of(
            "grad", "gradient",
            "zgap", "zgapmask", "rgbmask",
            "gamma", "gamma1_4"
    );

    /**
     * @param libraries
     * @return the list of libraries that contains only the top level libraries that are not variants of any other library
     * or variants for which the parent is not present in the source libraries parameter.
     */
    public static List<ColorDepthLibrary> collectLibrariesWithVariants(List<ColorDepthLibrary> libraries) {
        return librariesWithVariantsStream(libraries, new LinkedHashMap<>(), new LinkedHashSet<>());
    }

    private static List<ColorDepthLibrary> librariesWithVariantsStream(List<ColorDepthLibrary> libraries,
                                                                       Map<Reference, ColorDepthLibrary> collectedLibraries,
                                                                       Set<Reference> collectedVariants) {
        return libraries.stream()
                .peek(l -> {
                    collectedLibraries.put(Reference.createFor(l), l);
                    if (l.isVariant()) {
                        collectedVariants.add(Reference.createFor(l));
                    }
                })
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        libList -> libList.stream()
                                .filter(l -> {
                                    if (l.isVariant() && collectedLibraries.containsKey(l.getParentLibraryRef())) {
                                        // if the current library is a variant and the parent is in the list - filter it out
                                        // but before that make sure the variant is added to the parent's variant list
                                        collectedLibraries.get(l.getParentLibraryRef()).addLibraryVariant(l);
                                        return false;
                                    } else {
                                        return true;
                                    }
                                })
                                .collect(Collectors.toList())))
                ;
    }

    public static Set<ColorDepthLibrary> getSearchableVariants(ColorDepthLibrary colorDepthLibrary) {
        return colorDepthLibrary.getLibraryVariants().stream()
                .filter(l -> !isNonSearchableVariant(l.getVariant()))
                .collect(Collectors.toSet());
    }

    private static boolean isNonSearchableVariant(String variant) {
        return NON_SEARCHABLE_VARIANTS.contains(variant.toLowerCase());
    }

    public static Set<ColorDepthLibrary> selectVariants(ColorDepthLibrary colorDepthLibrary, Set<String> variants) {
        return colorDepthLibrary.getLibraryVariants().stream()
                .filter(l -> variants.contains(l.getVariant().toLowerCase()))
                .collect(Collectors.toSet());
    }

}
