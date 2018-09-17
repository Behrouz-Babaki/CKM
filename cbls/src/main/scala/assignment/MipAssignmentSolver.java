package assignment;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class MipAssignmentSolver {

	public Boolean hasImproved = false;
	public int[] assignments = null;
	int n;
	int k;
	double[] weights;
	int minClusterSize;
	int maxClusterSize;
	double minClusterWeight;
	double maxClusterWeight;

	public MipAssignmentSolver(int n, int k, double[] weights, int minClusterSize, int maxClusterSize,
			double minClusterWeight, double maxClusterWeight) {
		this.n = n;
		this.k = k;
		this.weights = weights;
		this.minClusterSize = minClusterSize;
		this.maxClusterSize = maxClusterSize;
		this.minClusterWeight = minClusterWeight;
		this.maxClusterWeight = maxClusterWeight;
	}

	public void solve(double[][] distances, int[] previousAssignments) {
		try {
			GRBEnv env = new GRBEnv("cluster");
			GRBModel model = new GRBModel(env);

			GRBVar[][] x = new GRBVar[n][k];

			for (int i = 0; i < n; i++)
				for (int j = 0; j < k; j++)
					x[i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x" + i + "-" + j);

			GRBLinExpr expr;

			for (int i = 0; i < n; i++) {
				expr = new GRBLinExpr();
				for (int j = 0; j < k; j++)
					expr.addTerm(1.0, x[i][j]);
				model.addConstr(expr, GRB.EQUAL, 1.0, "c1-" + i);
			}

			for (int j = 0; j < k; j++) {
				expr = new GRBLinExpr();
				for (int i = 0; i < n; i++)
					expr.addTerm(1.0, x[i][j]);
				model.addConstr(expr, GRB.GREATER_EQUAL, minClusterSize, "c2-" + j);
				model.addConstr(expr, GRB.LESS_EQUAL, maxClusterSize, "c3-" + j);
			}

			for (int j = 0; j < k; j++) {
				expr = new GRBLinExpr();
				for (int i = 0; i < n; i++)
					expr.addTerm(weights[i], x[i][j]);
				model.addConstr(expr, GRB.GREATER_EQUAL, minClusterWeight, "c4-" + j);
				model.addConstr(expr, GRB.LESS_EQUAL, maxClusterWeight, "c5-" + j);
			}

			if (previousAssignments != null) {
				double ub = 0;
				for (int i = 0; i < n; i++)
					ub += distances[i][previousAssignments[i]];
				System.out.println("ub: " + ub);
				expr = new GRBLinExpr();
				for (int i = 0; i < n; i++)
					for (int j = 0; j < k; j++)
						expr.addTerm(distances[i][j], x[i][j]);
				model.addConstr(expr, GRB.LESS_EQUAL, ub, "c-ub");
			}

			model.setObjective(new GRBLinExpr());
			model.optimize();

			if (model.get(GRB.IntAttr.Status) == GRB.Status.OPTIMAL) {
				hasImproved = true;
				assignments = new int[n];
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < k; j++)
						if (x[i][j].get(GRB.DoubleAttr.X) > 0.5)
							assignments[i] = j;
				}
			} else {
				assert (model.get(GRB.IntAttr.Status) == GRB.Status.INFEASIBLE);
			}

			model.dispose();
			env.dispose();

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}

}
