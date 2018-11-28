package org.janelia.configutils;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Set;

public class ConfigValueResolver {

    private final static char START_PLACEHOLDER = '{';
    private final static char END_PLACEHOLDER = '}';
    private final static char ESCAPE_CHAR = '\\';

    private enum ResolverState {
        OutsidePlaceHolder, InsidePlaceHolder, EscapeChar
    }

    public String resolve(String v, Map<String, String> context) {
        if (context == null || context.isEmpty()) {
            // no context so simply return the value as is
            return v;
        }
        return resolve(v, context, ImmutableSet.of());
    }

    private String resolve(String v, Map<String, String> context, Set<String> evalHistory) {
        if (StringUtils.isBlank(v)) return v;
        StringBuilder resolvedValueBuilder = new StringBuilder();
        StringBuilder placeHolderBuilder = new StringBuilder();
        ResolverState state = ResolverState.OutsidePlaceHolder;
        for (char currentChar : v.toCharArray()) {
            switch (state) {
                case OutsidePlaceHolder:
                    switch (currentChar) {
                        case ESCAPE_CHAR:
                            state = ResolverState.EscapeChar;
                            break;
                        case START_PLACEHOLDER:
                            placeHolderBuilder.append(currentChar);
                            state = ResolverState.InsidePlaceHolder;
                            break;
                        default:
                            resolvedValueBuilder.append(currentChar);
                            break;
                    }
                    break;
                case InsidePlaceHolder:
                    switch (currentChar) {
                        case END_PLACEHOLDER:
                            placeHolderBuilder.append(currentChar);
                            String placeHolderString = placeHolderBuilder.toString();
                            String placeHolderKey = placeHolderBuilder.substring(1, placeHolderBuilder.length() - 1);
                            if (evalHistory.contains(placeHolderKey)) {
                                throw new IllegalStateException("Circular dependency found while evaluating " + v + " -> " + evalHistory);
                            }
                            String placeHolderValue = context.get(placeHolderKey);
                            if (placeHolderValue == null) {
                                // no value found - put the placeholder as is
                                resolvedValueBuilder.append(placeHolderString);
                            } else {
                                resolvedValueBuilder.append(resolve(placeHolderValue, context, ImmutableSet.<String>builder().addAll(evalHistory).add(placeHolderKey).build()));
                            }
                            placeHolderBuilder.setLength(0);
                            state = ResolverState.OutsidePlaceHolder;
                            break;
                        default:
                            placeHolderBuilder.append(currentChar);
                            break;
                    }
                    break;
                case EscapeChar:
                    resolvedValueBuilder.append(currentChar);
                    state = ResolverState.OutsidePlaceHolder;
                    break;
            }
        }
        if (state == ResolverState.InsidePlaceHolder) {
            throw new IllegalStateException("Unclosed placeholder found while trying to resolve " + v + " -> " + evalHistory);
        }
        return resolvedValueBuilder.toString();
    }

}
