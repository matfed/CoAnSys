package pl.edu.icm.coansys.disambiguation.clustering.strategies;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.edu.icm.coansys.commons.java.StackTraceExtractor;

import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;

@SuppressWarnings("unused")
public class CompleteLinkageHACStrategy_StateOfTheArtTest {

	private static final Logger logger = LoggerFactory
			.getLogger(CompleteLinkageHACStrategy_StateOfTheArtTest.class);

	@Test
	public void emptyTest() {
		float[][] in = null;
		try {
			new CompleteLinkageHACStrategy_StateOfTheArt().clusterize(in);
			Assert.fail();
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(true);
		}
	}

	@Test
	public void oneElementTest() {
		float[][] in = { {} };
		int[] out = clusterData(in);

		String outS = Joiner.on(" ").join(Ints.asList(out));
		Assert.assertEquals("Elements are not correctly assigned", "0", outS);
		System.out.println("One element in one group");
		System.out.println(outS);
	}

	@Test
	public void twoSameElementsTest() {
		float[][] in = { {}, { 1 } };
		int[] out = clusterData(in);

		String outS = Joiner.on(" ").join(Ints.asList(out));
		Assert.assertEquals("Elements are not correctly assigned", "0 0", outS);
		System.out.println("Two elements in the same group");
		System.out.println(outS);
	}

	@Test
	public void twoNotSameElementsTest() {
		float[][] in = { {}, { -1 } };
		int[] out = clusterData(in);

		String outS = Joiner.on(" ").join(Ints.asList(out));
		Assert.assertEquals("Elements are not correctly assigned", "0 1", outS);
		System.out.println("Two elements in the saparate groups");
		System.out.println(outS);
	}

	@Test
	public void diffTest() {
		float[][] in = { {}, { -1 }, { -1, -1 }, { -1, -1, -1 },
				{ -1, -1, -1, -1 } };
		int[] out = clusterData(in);

		String outS = Joiner.on(" ").join(Ints.asList(out));
		Assert.assertEquals("Elements are not correctly assigned", "0 1 2 3 4",
				outS);
		System.out.println("Five elements in different groups");
		System.out.println(outS);
	}

	@Test
	public void allTheSameTest() {
		float[][] in = { {}, { 1 }, { 1, 1 }, { 1, 1, 1 }, { 1, 1, 1, 1 } };
		int[] out = clusterData(in);

		String outS = Joiner.on(" ").join(Ints.asList(out));
		Assert.assertEquals("Elements are not correctly assigned", "4 4 4 4 4",
				outS);
		System.out.println("Five objects in the same group");
		System.out.println(outS);
	}

	@Test
	public void smallExampleTest() {
		float[][] in = { {}, { 15 }, { -46, -3 }, { -2, -18, -20 },
				{ -100, -100, -3, -200 } };
		int[] out = clusterData(in);

		String outS = Joiner.on(" ").join(Ints.asList(out));
		Assert.assertEquals("Elements are not correctly assigned", "0 0 2 3 4",
				outS);
		System.out.println("Four separate groups");
		System.out.println(outS);
	}

	@Test
	public void oneCluster() {
		float[][] in = { {}, { 15 }, { 15, 15 } };
		int[] out = clusterData(in);
		String outS = Joiner.on(" ").join(Ints.asList(out));
		System.out.println("Three elements in the same group");
		System.out.println(outS);
	}

	@Test
	public void oneCluster2() {
		float[][] in = { {}, { -1 }, { -1, -1 } };
		int[] out = clusterData(in);
		String outS = Joiner.on(" ").join(Ints.asList(out));
		System.out.println("Three elements in different groups");
		System.out.println(outS);
	}

	// @Test
	public void mtcarsDistTest() throws Exception {
		float[][] in = readResourceToFloatArray("mtcars.dist.csv");
		// float[][] in = readResourceToFloatArray("eurodist.dist.csv");
		// Instances data = readResourceToInstances("mtcars.base.csv");

		// for(float[] line : in){
		// for(float n : line){
		// System.out.print(n+";");
		// }
		// System.out.println();
		// }

		int[] out = null;

		// System.out.println("======== is now ==========");
		out = clusterData(in);
		printClusters(out, in.length);
		// System.out.println("======== weka -- should be ==========");
		// out = wekaTest(data);
		// printClusters(out, in.length);
		// System.out.println("======== hclust@R -- should be ======");
		// out = new int[]{ 1, 1, 1, 2, 3, 2, 3, 1, 1, 1, 1, 2, 2, 2, 4, 4, 4,
		// 5, 5, 5, 1, 2, 2, 3, 3, 5, 1, 1, 3, 6, 7, 1 };
		// printClusters(out, in.length);
	}

	protected float[][] readResourceToFloatArray(String path)
			throws IOException {
		InputStream is = CompleteLinkageHACStrategy_StateOfTheArtTest.class
				.getClassLoader().getResourceAsStream(path);
		String indata = IOUtils.toString(is);

		float maxFloat = Float.MIN_VALUE;
		String[] testLines = indata.split("\n");
		for (String s : testLines) {
			String[] numbers = s.split(",");
			for (String n : numbers) {
				float f = Float.parseFloat(n);
				maxFloat = Math.max(maxFloat, f);
			}
		}

		// System.out.println("============ "+maxFloat);

		String[] lines = indata.split("\n");
		float[][] in = new float[lines.length][];
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			String[] values = line.split(",");

			float[] distLine = new float[i];
			for (int j = 0; j < i; j++) {
				distLine[j] = /* maxFloat- */Float.parseFloat(values[j]);
			}
			in[i] = distLine;
		}
		return in;
	}

	protected void printClusters(int[] out, int length) {
		String outS = Joiner.on(" ").join(Ints.asList(out));
		// System.out.println("Ids of clusters assigned: " + outS + "\n");
		System.out.println("Pairs <clusterId, [item]>");
		int[][] split = splitIntoClusters(out, length);
		int j = 0;
		for (int i = 0; i < split.length; i++) {
			if (split[i] == null) {
				continue;
			}
			System.out.println(j + ": "
					+ Joiner.on(" ").join(Ints.asList(split[i])));
			j++;
		}
	}

	protected int[][] splitIntoClusters(int[] clusterAssociation, int N) {
		// cluster[ cluster id ] = array with contributors' simIds
		int[][] clusters = new int[N][];
		int[] index = new int[N];
		int[] clusterSize = new int[N];
		assert (clusterAssociation.length == N);

		// preparing clusters' sizes
		for (int i = 0; i < N; i++) {
			clusterSize[clusterAssociation[i]]++;
		}
		// reserving memory
		for (int i = 0; i < N; i++) {
			if (clusterSize[i] > 0) {
				clusters[i] = new int[clusterSize[i]];
			} else {
				clusters[i] = null;
			}

			index[i] = 0;
		}
		// filling clusters
		int id;
		for (int i = 0; i < N; i++) {
			id = clusterAssociation[i];
			clusters[id][index[id]] = i;
			index[id]++;
		}
		return clusters;
	}

	protected int[] clusterData(float[][] in) {
		int[] out = null;
		try {
			out = new CompleteLinkageHACStrategy_StateOfTheArt().clusterize(in);
		} catch (Exception e) {
			logger.error(StackTraceExtractor.getStackTrace(e));
			Assert.fail();
		}
		return out;
	}
}
