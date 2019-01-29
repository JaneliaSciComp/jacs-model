package org.janelia.model.access.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Common utility methods for dealing with channel specification strings.
 * 
 * A channel specification is a string with a single character per channel. Each character represents 
 * the content of the channel, either signal ('s') or reference ('r'). Generally, there is exactly one
 * reference channel in an image, so an n-channel image has n-1 signal channels and 1 reference channel. 
 * 
 * Certain pipelines want channel content to be specified in different ways. For example, by providing indexes 
 * of the reference channel and/or signal channels, indexed from either 0 or 1 (we have no common standard 
 * for this currently). This utility class provides methods for deriving converting to and from
 * channel specifications ("chanSpec") and other formats such as index lists. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class ChanSpecUtils {

	public static final char REFERENCE = 'r';
	public static final char SIGNAL = 's';
    
	/**
	 * Given the number of channels and a 1-indexed reference channel, create a channel specification. 
	 * For example, with a 4 channel image, with refChannel at 4, the chanSpec would be "sssr".
	 * @param numChannels number of channels in the image (length of the resulting chanSpec)
	 * @param refChannel which channel is the reference channel (1-indexed)
	 * @return
	 */
    public static String createChanSpec(int numChannels, int refChannel) {
		StringBuilder sb = new StringBuilder();
		for(int i=1; i<=numChannels; i++) {
			if (i==refChannel) {
				sb.append(REFERENCE);
			}
			else {
				sb.append(SIGNAL);
			}
		}
		return sb.toString();
	}

    /**
     * Convert from a channel specification (e.g. "ssr") to a list of unique channel identifiers (e.g. ["s0","s1","r0"])
     * @param chanSpec a channel specification (e.g. "ssr")
     * @return a list of unique identifiers, each prepended with either "s" for signal or "r" for reference.
     */
    public static List<String> convertChanSpecToList(String chanSpec) { 
        int s = 0;
        int r = 0;
        List<String> channelList = new ArrayList<>();
        for(int sourceIndex=0; sourceIndex<chanSpec.length(); sourceIndex++) {
            char imageChanCode = chanSpec.charAt(sourceIndex);
            switch (imageChanCode) {
            case SIGNAL:
                channelList.add(SIGNAL+""+s);
                s++;
                break;
            case REFERENCE:
                channelList.add(REFERENCE+""+r);
                r++;
                break;
            default:
                throw new IllegalStateException("Unknown channel code: "+imageChanCode);
            }
        }
        return channelList;
    }

    /**
     * Convert from a list of channel identifiers (e.g. ["s0","s1","r0"]) to a channel specification (e.g. "ssr").
     * @param chanList a list of unique identifiers, each prepended with either "s" for signal or "r" for reference.
     * @return a channel specification (e.g. "ssr")
     */
    public static String convertListToChanSpec(List<String> chanList) {

        StringBuilder chanSpec = new StringBuilder();
        for (String s : chanList) {
            chanSpec.append(s.charAt(0));
        }

        return chanSpec.toString();
    }

    /**
     * Get a comma-separated list of one-indexed channel indexes of reference channels (normally just one) in a given channel specification.
     * @param chanSpec a channel specification (e.g. "ssr")
     * @return index of reference channels (e.g. "3")
     */
    public static String getReferenceChannelCSV(String chanSpec) {
    	return getChannelIndexes(chanSpec, REFERENCE, ',', 1);
    }

    /**
     * Get a comma-separated list of one-indexed channel indexes of signal channels in a given channel specification.
     * @param chanSpec a channel specification (e.g. "ssr")
     * @return index of signal channels (e.g. "1,2")
     */
    public static String getSignalChannelCSV(String chanSpec) {
    	return getChannelIndexes(chanSpec, SIGNAL, ',', 1);
    }

    /**
     * Returns a space-delimited list of zero-indexed channel indexes of reference channels (normally just one) in a given channel specification.
     * @param channelSpec channel specification (e.g. "ssr")
     * @return zero-indexed reference channels (e.g. "2")
     */
    public static String getReferenceChannelIndexes(String channelSpec) {
        return getChannelIndexes(channelSpec, REFERENCE, ' ', 0);
    }

    /**
     * Returns a space-delimited list of zero-indexed channel indexes of signal channels in a given channel specification.
     * @param channelSpec channel specification (e.g. "rsss")
     * @return zero-indexed signal channels (e.g. "1 2 3")
     */
    public static String getSignalChannelIndexes(String channelSpec) {
        return getChannelIndexes(channelSpec, SIGNAL, ' ', 0);
    }
    
    /**
     * Method for converting channel specifications into various formats needed by pipelines.
     * @param chanSpec a channel specification (e.g. "ssr")
     * @param chanCode the channel codes to get indexes for
     * @param separator the separator between indexes (e.g. comma, space, etc)
     * @param startIndex the index to start numbering channels with (e.g. 0 or 1) 
     * @return
     */
    private static String getChannelIndexes(String chanSpec, final char chanCode,  final char separator, final int startIndex) {
    	if (chanSpec==null) return "";
    	StringBuilder sb = new StringBuilder();
        for(int sourceIndex=0; sourceIndex<chanSpec.length(); sourceIndex++) {
        	int chanIndex = sourceIndex+startIndex;
            if (chanSpec.charAt(sourceIndex) == chanCode) {
            	if (sb.length()>0) sb.append(separator);
            	sb.append(chanIndex);
            }
        }
        return sb.toString();
    }

    public static List<Integer> getReferenceChannelIndexList(String channelSpec) {
        return getChannelIndexList(channelSpec, REFERENCE, 0);
    }

    public static List<Integer> getSignalChannelIndexList(String channelSpec) {
        return getChannelIndexList(channelSpec, SIGNAL, 0);
    }

    private static List<Integer> getChannelIndexList(String chanSpec, final char chanCode, final int startIndex) {
        List<Integer> indexes = new ArrayList<>();
        if (chanSpec==null) return indexes;
        for(int sourceIndex=0; sourceIndex<chanSpec.length(); sourceIndex++) {
            int chanIndex = sourceIndex+startIndex;
            if (chanSpec.charAt(sourceIndex) == chanCode) {
                indexes.add(chanIndex);
            }
        }
        return indexes;
    }

    /**
     * Fiji defines color channels as follows: (R)ed, (G)reen, (B)lue, grey(1), (C)yan, (M)agenta, (Y)ellow
     * We also control a divisor (inverse brightness, where 1 is brightest) that can be used to control the 
     * color when it is used for a reference channel. 
     * @param hexColor
     * @param channelType
     * @return
     */
    public static FijiColor getColorCode(String hexColor, char channelType) {
        if ("#ff0000".equals(hexColor)) {
            return new FijiColor('R',channelType=='r' ? 3 : 1); // Red
        }
        else if ("#00ff00".equals(hexColor)) {
            return new FijiColor('G',channelType=='r' ? 2 : 1); // Green
        }
        else if ("#0000ff".equals(hexColor)) {
            return new FijiColor('B',channelType=='r' ? 1 : 1); // Blue
        }
        else if ("#ffffff".equals(hexColor)) {
            return new FijiColor('1',channelType=='r' ? 2 : 1); // Grey
        }
        else if ("#0000ff".equals(hexColor)) {
            return new FijiColor('C',channelType=='r' ? 2 : 1); // Cyan
        }
        else if ("#ff00ff".equals(hexColor)) {
            return new FijiColor('M',channelType=='r' ? 2 : 1); // Magenta
        }
        else if ("#ffff00".equals(hexColor)) {
            return new FijiColor('Y',channelType=='r' ? 2 : 1); // Yellow
        }
        else if ("#7e5200".equals(hexColor)) {
            return new FijiColor('Y',channelType=='r' ? 3 : 2); // Brown
        }
        return new FijiColor('?',1);
    }
    
    /**
     * Returns the default color spec for the given chanspec. 
     * By default, we assign RGB to the first 3 signal channels, and white to the reference channel.
     * If there are more than 3 signal channels, this method assigns the color as '?'. 
     * @param chanSpec channel specification (e.g. "ssr")
     * @return color specification (e.g. "RG1")
     */
    public static String getDefaultColorSpec(String chanSpec, String signalColors, String referenceColor) {

        List<String> tags = new ArrayList<String>();
        for(int i=0; i<signalColors.length(); i++) {
            tags.add(signalColors.substring(i, i+1).toUpperCase());
        }
        
        StringBuilder csb = new StringBuilder();
        
        for(int i=0; i<chanSpec.length(); i++) {
            char type = chanSpec.charAt(i);
            if (type=='r') {
                csb.append(referenceColor);
            }
            else {
                if (tags.isEmpty()) {
                    csb.append("?");
                }
                else {
                    csb.append(tags.remove(0));
                }
            }
        }
        
        return csb.toString();
    }
}
