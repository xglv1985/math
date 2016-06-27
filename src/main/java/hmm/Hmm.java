package hmm;

import java.util.HashMap;
import java.util.Map;

/**
 * 隐马尔可夫模型
 * @author xuguanglv
 *
 */
public class Hmm {
	//初始概率向量
//	private static double[] pai = {0.63, 0.17, 0.20};
	private static double[] pai = {0.333, 0.333, 0.333};
	
	//状态转移矩阵
//	private static double[][] A = {{0.500, 0.375, 0.125},
//							        {0.250, 0.125, 0.625},
//							        {0.250, 0.375, 0.375}};
	private static double[][] A = {{0.333, 0.333, 0.333},
							        {0.333, 0.333, 0.333},
							        {0.333, 0.333, 0.333}};
	
	//混淆矩阵
//	private static double[][] B = {{0.60, 0.20, 0.15, 0.05},
//							        {0.25, 0.25, 0.25, 0.25},
//							        {0.05, 0.10, 0.35, 0.50}};
	private static double[][] B = {{0.5, 0.5},
							        {0.75, 0.25},
							        {0.25, 0.75}};
	
	//隐藏状态索引
	private static Map<Integer, String> hiddenStateIndex = new HashMap<Integer, String>();
	static{
		hiddenStateIndex.put(0, "S(0)");
		hiddenStateIndex.put(1, "S(1)");
		hiddenStateIndex.put(2, "S(2)");
	}
	
	//观察状态索引
	private static Map<String, Integer> observableStateIndex = new HashMap<String, Integer>();
	static{
		observableStateIndex.put("O(0)", 0);
		observableStateIndex.put("O(1)", 1);
//		observableStateIndex.put("O(2)", 2);
//		observableStateIndex.put("O(3)", 3);
	}
	
	//前向算法 根据观察序列和已知的隐马尔可夫模型 返回这个模型生成这个观察序列的概率
	//alpha[t][j]表示t时刻由隐藏状态S(j)生成观察状态O(t)的概率
	public static double forward(String[] observedSequence){
		double[][] alpha = new double[observedSequence.length][A.length];
		
		//利用动态规划计算出alpha数组
		//初始化
		for(int i = 0; i <= A.length - 1; i++){
			int index = observableStateIndex.get(observedSequence[0]);
			alpha[0][i] = pai[i] * B[i][index];
		}
		for(int t = 1; t <= observedSequence.length - 1; t++){
			for(int j = 0; j <= A.length - 1; j++){
				double sum = 0;
				for(int i = 0; i <= A.length - 1; i++){
					sum += (alpha[t - 1][i] * A[i][j]);
				}
				int index = observableStateIndex.get(observedSequence[t]);
				alpha[t][j] = sum * B[j][index];
			}
		}
		double prob = 0;
		for(int i = 0; i <= A.length - 1; i++){
			prob += alpha[observedSequence.length - 1][i];
		}
		return prob;
	}
	
	//维特比算法 根据观察状态序列和已知的隐马尔可夫模型 返回最可能的隐藏状态序列
	//delta[t][j]表示t时刻最可能生成O(0) O(1) ... O(t)的以状态j为结尾的隐藏状态序列出现的概率
	public static String[] viterbi(String[] observedSequence){
		String[] hiddenSequence = new String[observedSequence.length];
		double[][] delta = new double[observedSequence.length][A.length];
		//一个前驱数组 predecessor[t][j]表示以状态j为结尾的概率最大的隐藏状态序列中j的前一个状态
		int[][] predecessor = new int[observedSequence.length][A.length];
		
		//利用动态规划计算出delta数组
		//初始化
		for(int i = 0; i <= A.length - 1; i++){
			int index = observableStateIndex.get(observedSequence[0]);
			delta[0][i] = pai[i] * B[i][index];
		}
		for(int i = 0; i <= A.length - 1; i++){
			predecessor[0][i] = -1;
		}
		for(int t = 1; t <= observedSequence.length - 1; t++){
			for(int j = 0; j <= A.length - 1; j++){
				double max = 0;
				for(int i = 0; i <= A.length - 1; i++){
					if(delta[t - 1][i] * A[i][j] > max){
						max = delta[t - 1][i] * A[i][j];
						predecessor[t][j] = i;
					}
				}
				int index = observableStateIndex.get(observedSequence[t]);
				delta[t][j] = max * B[j][index];
			}
		}
		//max就是生成整个观察状态序列的最可能的隐藏状态序列的概率
		//lastHiddenIndex用来表示这最可能的隐藏状态序列的最后一个隐藏状态
		double max = 0;
		int lastHiddenIndex = 0;
		for(int i = 0; i <= A.length - 1; i++){
			if(delta[observedSequence.length - 1][i] > max){
				max = delta[observedSequence.length - 1][i];
				lastHiddenIndex = i;
			}
		}
		//回溯出最可能生成观察状态序列的隐藏状态序列的每一个隐藏状态 放入hiddenSequence
		hiddenSequence[observedSequence.length - 1] = hiddenStateIndex.get(lastHiddenIndex);
		int curHiddenIndex = lastHiddenIndex;
		for(int t = observedSequence.length - 1; t >= 1; t--){
			hiddenSequence[t - 1] = hiddenStateIndex.get(predecessor[t][curHiddenIndex]);
			curHiddenIndex = predecessor[t][curHiddenIndex];
		}
		
		return hiddenSequence;
	}
	
	public static void main(String[] args){
		String[] observedSequence = {"O(0)", "O(0)", "O(0)", "O(0)", "O(1)", "O(0)", "O(1)", "O(1)", "O(1)", "O(1)"};
		System.out.println(forward(observedSequence));
		String[] hiddenSequence = viterbi(observedSequence);
		for(String hiddenState : hiddenSequence){
			System.out.print(hiddenState + " ");
		}
	}
}
