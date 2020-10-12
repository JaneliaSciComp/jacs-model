package org.janelia.model.domain.gui.cdmip;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
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
                .filter(l -> isSearchableVariant(l.getVariant()))
                .collect(Collectors.toSet());
    }

    public static boolean isSearchableVariant(String variant) {
        return StringUtils.isBlank(variant) || !NON_SEARCHABLE_VARIANTS.contains(variant.toLowerCase());
    }

    /**
     * Select the matching variants if the library has variants or if it is a variant already return a set
     * of possible variants.
     * @param colorDepthLibrary
     * @param variants
     * @return
     */
    public static Set<ColorDepthLibrary> selectVariantCandidates(ColorDepthLibrary colorDepthLibrary, Set<String> variants) {
        if (colorDepthLibrary.isVariant() && variants.contains(colorDepthLibrary.getVariant().toLowerCase())) {
            return Collections.singleton(colorDepthLibrary);
        } else {
            return variants.stream()
                    .map(variant -> colorDepthLibrary.getLibraryVariant(variant)
                            .orElseGet(() -> {
                                ColorDepthLibrary variantLibrary = new ColorDepthLibrary();
                                String variantLibraryId;
                                if (colorDepthLibrary.isVariant()) {
                                    variantLibraryId = colorDepthLibrary.getIdentifier().replace(colorDepthLibrary.getVariant(), variant);
                                } else {
                                    variantLibraryId = colorDepthLibrary.getIdentifier() + "_" + variant;
                                }
                                variantLibrary.setIdentifier(variantLibraryId);
                                variantLibrary.setName(variantLibraryId);
                                variantLibrary.setVariant(variant);
                                variantLibrary.setParentLibraryRef(colorDepthLibrary.getParentLibraryRef());
                                return variantLibrary;
                            }))
                    .collect(Collectors.toSet());
        }
    }

}
