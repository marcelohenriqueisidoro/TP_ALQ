import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class SimulationMaladie {

    private static final int TAILLE_GRILLE = 300;
    private static final int NOMBRE_INDIVIDUS = 20000;
    private static final int NOMBRE_SIMULATIONS = 100;
    private static final int NOMBRE_ITERATIONS = 730;

    private static char[][] grille;
    private static Random prng;

    private static final double FORCE_INFECTION = 0.8;

    public static void main(String[] args) {
        prng = new Random(10);

        for (int sim = 1; sim <= NOMBRE_SIMULATIONS; sim++) {
            prng = new Random(10);
            initialiserSimulation();

            // Durées des états E, I, R
            double DUREE_E = negExp(3);
            double DUREE_I = negExp(7);
            double DUREE_R = negExp(365);

            try {
                FileWriter writer = new FileWriter("resultats_simulation_" + sim + ".tsv");

                writer.write("Iteration\tS\tE\tI\tR\n");

                for (int i = 0; i < NOMBRE_ITERATIONS; i++) {
                    executerIteration(DUREE_E, DUREE_I, DUREE_R);
                    int[] comptage = compterEtats();
                    writer.write(i + "\t" + comptage[0] + "\t" + comptage[1] + "\t" + comptage[2] + "\t" + comptage[3] + "\n");
                }

                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void initialiserSimulation() {
        grille = new char[TAILLE_GRILLE][TAILLE_GRILLE];

        int numInfectes = 20;
        int numSusceptibles = NOMBRE_INDIVIDUS - numInfectes;

        for (int i = 0; i < numInfectes; i++) {
            int x = prng.nextInt(TAILLE_GRILLE);
            int y = prng.nextInt(TAILLE_GRILLE);

            grille[x][y] = 'I';
            grille[x][(y + 1) % TAILLE_GRILLE] = '1';
        }

        for (int i = 0; i < numSusceptibles; i++) {
            int x = prng.nextInt(TAILLE_GRILLE);
            int y = prng.nextInt(TAILLE_GRILLE);

            if (grille[x][y] == 0) {
                grille[x][y] = 'S';
            } else {
                i--;
            }
        }
    }

    private static int[] compterEtats() {
        int[] comptage = new int[4];

        for (int i = 0; i < TAILLE_GRILLE; i++) {
            for (int j = 0; j < TAILLE_GRILLE; j++) {
                char etat = grille[i][j];
                if (etat == 'S') comptage[0]++;
                else if (etat == 'E') comptage[1]++;
                else if (etat == 'I') comptage[2]++;
                else if (etat == 'R') comptage[3]++;
            }
        }

        return comptage;
    }

    private static void executerIteration(double DUREE_E, double DUREE_I, double DUREE_R) {
        for (int i = 0; i < TAILLE_GRILLE; i++) {
            for (int j = 0; j < TAILLE_GRILLE; j++) {
                char etat = grille[i][j];
                int temps = Character.getNumericValue(grille[i][(j + 1) % TAILLE_GRILLE]);

                if (etat == 'E') {
                    if (temps > DUREE_E) {
                        grille[i][j] = 'I';
                    } else {
                        grille[i][(j + 1) % TAILLE_GRILLE]++;
                    }
                } else if (etat == 'I') {
                    if (temps > DUREE_I) {
                        grille[i][j] = 'R';
                    } else {
                        grille[i][(j + 1) % TAILLE_GRILLE]++;
                    }
                } else if (etat == 'R') {
                    if (temps > DUREE_R) {
                        grille[i][j] = 'S';
                    } else {
                        grille[i][(j + 1) % TAILLE_GRILLE]++;
                    }
                } else if (etat == 'S') {
                    infecterVizinhos(i, j);
                }
            }
        }
    }

    private static void infecterVizinhos(int i, int j) {
        int numInfectes = 0;
        //definition des voisins
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1, -1, 1, -1, 1, -1, 1, 0, 0};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1, -1, -1, 1, 1, 0, 0, -1, 1};


        for (int k = 0; k < 16; k++) {
            int ni = (i + dx[k] + TAILLE_GRILLE) % TAILLE_GRILLE;
            int nj = (j + dy[k] + TAILLE_GRILLE) % TAILLE_GRILLE;

            if (grille[ni][nj] == 'I') {
                numInfectes++;
            }
        }

        double p = 1 - Math.exp(-FORCE_INFECTION * numInfectes);
        double aleatoire = prng.nextDouble();

        if (aleatoire < p) {
            grille[i][j] = 'E';
            grille[i][(j + 1) % TAILLE_GRILLE] = '1';
        }
    }

    private static double negExp(double inMean) {
        return -inMean * Math.log(1 - prng.nextDouble());
    }
}

