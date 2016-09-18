/**
 * 
 * PROJET : Simulation d'un feu de for�t JAVA
 * AUTEURS : COURGEY Florian - GU�NARD Thomas
 * ANN�E : 2014
 * �COLE : EPF �cole d'ing�nieurs
 * 
 * Fonctionnalit�s (non ordonn�es) :
 * - terrain g�n�r� par heightmap
 * - Extincteur pour �teindre le feu
 * - historique de modifications avec retour arri�re (comme un CTRL Z)
 * - dessin simple mais avanc�
 * - pas d'horloge au clic ou au temps
 * - sauvegarde/chargement carte
 * - vent param�trable en intensit� et direction
 * 
 * ORGANISATION :
 * le fichier Main.java contient le main qui lance uniquement une nouvelle Fenetre de Fenetre.java
 * la Fenetre est l'unique JFrame du programme, tout se passe dedans
 * Elle contient surtout la Carte de Carte.java
 * et cette Carte fait appel � toutes les fonctionnalit�s puisqu'elle contient
 * une matrice de Case de Case.java
 * une Heightmap de Heightmap.java
 * un Vent de Vent.java
 * des Extincteurs de Extincteur.java
 * 
 */

/**
 * 
 * FICHIER : Heightmap.java
 * 
 * l'objet Heightmap nous permet de g�n�rer des zones de terrain pour plus de r�alisme
 * 
 * l'algorithme principal vient du site du zero, lien plus bas, car il est incompr�hensible et impossible de coder �a nous m�me
 * 
 * N�anmoins nous avons su nous l'approprier, le modifier et l'exploiter pleinement
 * 
 * 
 */

package entites;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.swing.JLabel;
import javax.swing.JPanel;

import entites.Carte.Vegetation;
import entites.Case.Nature;

public class Heightmap extends JPanel {
	private static final long serialVersionUID = 4081709997554048018L;
	
	private float unitaire = (float) (1.0f/Math.sqrt(2));
	private float gradient[][] = {{unitaire,unitaire},{-unitaire,unitaire},{unitaire,-unitaire},{-unitaire,-unitaire},{1,0},{-1,0},{0,1},{0,-1}};
	private int tableDePermutation[];
	
	private int[][] heightmap;
	private Nature[][] heightmapNature;
	
	//
	public static final float DEFAULT_ZOOM = 5;
	
	/**
	 * la heightmap g�n�re un bruit de perlin
	 * 
	 * puis le convertit en zones adapt�es � notre besoin : zone d'eau, de maisons, de plaines et de for�ts
	 * 
	 * @param carte
	 * @param zoom le zoom permet d'avoir des zones plus ou moins grandes
	 * @param vegetation
	 */
	public Heightmap(Carte carte, Float zoom, Vegetation vegetation){
		super();
		this.setVisible(true);
		this.setOpaque(true);
		// cr�ation du bruit de perlin
		if(zoom == null ){
			zoom = DEFAULT_ZOOM;
		}
		tableDePermutation = new int[512];
		for(int i=0 ; i<512 ; i++){
			tableDePermutation[i] = i;
		}
		shuffleArray(tableDePermutation);
		
		Case[][] grille = carte.getGrille();		
		this.setLayout(new GridLayout(grille.length, grille[0].length));

		
		// conversion en zones utilisables
		
		// ETAPE 1
		// on cr�e une premi�re heightmap dont les valeurs s'�tendent d'un certain minimum (appel� min)
		// � un certain maximum (appel� max)
		
		// ETAPE 2
		// puis on divise cet intervale selon le pourcentage de v�g�tation :
		// voir rapport
		
		// ETAPE 1
		heightmap = new int[grille.length][grille[0].length];
		ArrayList<Integer> l = new ArrayList<>();
		for(int i=0 ; i<grille.length ; i++){
			for(int j=0 ; j<grille[0].length ; j++){
				
				int pourcentage = (int)(((Get2DPerlinNoiseValue(i, j, zoom)+1)/2)*255);
				heightmap[i][j] = pourcentage;
				JLabel jla = new JLabel();
				jla.setBackground(new Color(pourcentage, pourcentage, pourcentage));
				jla.setOpaque(true);
				this.add(jla);
				l.add(pourcentage);
			}
		}
		
		// ETAPE 2
		Collections.sort(l);
		int min = l.get(0);
		int max = l.get(l.size()-1);
		
		int intervalleTotal = max-min+1;
		int intervalleVegetation = intervalleTotal*vegetation.getPourcentage()/100;
		
		heightmapNature = new Nature[grille.length][grille[0].length];
		for(int i=0 ; i<grille.length ; i++){
			for(int j=0 ; j<grille[0].length ; j++){
				// si c'est de la vegetation
				int valeurHeightmap = heightmap[i][j];
				Nature n;
				if(valeurHeightmap < min + intervalleVegetation){
					if(valeurHeightmap < min + intervalleVegetation/3){
						n = Nature.FORET;
					} 
					else if (valeurHeightmap < min + 2*intervalleVegetation/3) {
						n = Nature.PLAINE;
					}
					else {
						n = Nature.MAISON;
					}
				} 
				// sinon c'est de l'ininflammable
				else {
					n = Nature.EAU;
				}
				heightmapNature[i][j] = n;
			}
		}
		
		
	}
	
	public Nature[][] getHeightMapNature(){
		return heightmapNature;
	}
	
	/**
	 * algorithme de Perlin tir� du site du z�ro, incompr�hensible
	 * http://fr.openclassrooms.com/informatique/cours/bruit-de-perlin
	 * 
	 * @param x
	 * @param y
	 * @param res : resolution ou zoom
	 * @return
	 */
	float Get2DPerlinNoiseValue(float x, float y, float res){
	    float tempX,tempY;
	    int x0,y0,ii,jj,gi0,gi1,gi2,gi3;
	    float tmp,s,t,u,v,Cx,Cy,Li1,Li2;
	    
	    //Adapter pour la r�solution
	    x /= res;
	    y /= res;

	    //On r�cup�re les positions de la grille associ�e � (x,y)
	    x0 = (int)(x);
	    y0 = (int)(y);

	    //Masquage
	    ii = x0 & 255;
	    jj = y0 & 255;

	    //Pour r�cup�rer les vecteurs
	    // petite modification perso car le sien �tait indexOutOfBound une fois sur 3...
	    int indice0 = ii + tableDePermutation[jj];
	    while(indice0 > 511){indice0--;}
	    
	    int indice1 = ii + 1 + tableDePermutation[jj];
	    while(indice1 > 511){indice1--;}
	    
	    int indice2 = ii + tableDePermutation[jj + 1];
	    while(indice2 > 511){indice2--;}
	    
	    int indice3 = ii + 1 + tableDePermutation[jj + 1];
	    while(indice3 > 511){indice3--;}
	    
	    gi0 = tableDePermutation[indice0] % 8;
	    gi1 = tableDePermutation[indice1] % 8;
	    gi2 = tableDePermutation[indice2] % 8;
	    gi3 = tableDePermutation[indice3] % 8;

	    //on r�cup�re les vecteurs et on pond�re
	    tempX = x-x0;
	    tempY = y-y0;
	    s = gradient[gi0][0]*tempX + gradient[gi0][1]*tempY;

	    tempX = x-(x0+1);
	    tempY = y-y0;
	    t = gradient[gi1][0]*tempX + gradient[gi1][1]*tempY;

	    tempX = x-x0;
	    tempY = y-(y0+1);
	    u = gradient[gi2][0]*tempX + gradient[gi2][1]*tempY;

	    tempX = x-(x0+1);
	    tempY = y-(y0+1);
	    v = gradient[gi3][0]*tempX + gradient[gi3][1]*tempY;


	    //Lissage
	    tmp = x-x0;
	    Cx = 3 * tmp * tmp - 2 * tmp * tmp * tmp;

	    Li1 = s + Cx*(t-s);
	    Li2 = u + Cx*(v-u);

	    tmp = y - y0;
	    Cy = 3 * tmp * tmp - 2 * tmp * tmp * tmp;

	    return Li1 + Cy*(Li2-Li1);
	}
	
	// m�thode pour m�langer un tableau
	static void shuffleArray(int[] tab){
		Random rand = new Random();
		for (int i = tab.length - 1; i > 0; i--){
			int index = rand.nextInt(i + 1);
			int tmp = tab[index];
			tab[index] = tab[i];
			tab[i] = tmp;
		}
	}
}
