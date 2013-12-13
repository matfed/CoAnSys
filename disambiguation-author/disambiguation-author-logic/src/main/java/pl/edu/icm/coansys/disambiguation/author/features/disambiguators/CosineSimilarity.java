package pl.edu.icm.coansys.disambiguation.author.features.disambiguators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author dtkaczyk, pdendek
 */
@SuppressWarnings("boxing")
public class CosineSimilarity extends Disambiguator {

	public CosineSimilarity() {
		super();
	}

	public CosineSimilarity(double weight, double maxVal) {
		//maxVal - unused
		super(weight, maxVal);
	}

	@Override
	public double calculateAffinity(List<Object> f1, List<Object> f2) {
        Map<Object, Integer> v1 = calculateVector(f1);
        Map<Object, Integer> v2 = calculateVector(f2);

        return dotProduct(v1, v2) / (vectorLength(v1) * vectorLength(v2));
    }

    private Map<Object, Integer> calculateVector(List<Object> tokens) {
        HashMap<Object, Integer> vector = new HashMap<Object, Integer>();
        for (Object token : tokens) {
            if (vector.containsKey(token)) {
                vector.put(token, vector.get(token) + 1);
            } else {
                vector.put(token, 1);
            }
        }
        return vector;
    }

    private double vectorLength(Map<Object, Integer> vector) {
        double ret = 0.0;
        for (Entry<Object, Integer> entry : vector.entrySet()) {
            ret += entry.getValue() * entry.getValue();
        }
        return Math.sqrt(ret);
    }

	private double dotProduct(Map<Object, Integer> vector1, Map<Object, Integer> vector2) {
        double ret = 0.0;
        for (Entry<Object, Integer> entry : vector1.entrySet()) {
            if (vector2.containsKey(entry.getKey())) {
                ret += entry.getValue() * vector2.get(entry.getKey());
            }
        }
        return ret;
    }	
}
