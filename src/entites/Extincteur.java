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
 * FICHIER : Extincteur.java
 * 
 * les objets Extincteur apparaissent sur un jpanel superpos� au dessus de la Carte
 * 
 * ce sont des images qui transforment les cases br�l�es en cases vierges par un clic sur l'image
 * 
 * 
 */

package entites;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Extincteur extends JLabel implements Serializable {
	private static final long serialVersionUID = 4279980892652722454L;
	
	// dimensions icone
	final int HAUTEUR_ICONE = 100;
	final int LARGEUR_ICONE = 100;
	
	// ligne de la case li�e
	private int ligne;
	// colonne de la case li�e
	private int colonne;
	// grille de toutes les cases
	//	 forc�ment en attribut pour �tre atteignable depuis la classe anonyme
	protected Case[][] grille;
	// panel sur lequel ajouter l'extincteur
	private JPanel panel_transparent;
	
	
	public final Extincteur moi = this;
	
	/**
	 * 
	 * @param c case � laquelle l'extincteur est li�
	 * @param panel_transparent panel sur lequel ajouter l'ex
	 * @param grille grille de toutes les cases pour en �teindre quelques unes
	 * @param largeurCarte le nombre de cases en largeur pour positionner les extincteurs sur la carte
	 * @param hauteurCarte idem en hauteur
	 */
	public Extincteur(Case c, final JPanel panel_transparent, final Case[][] grille){
		super();
		this.ligne = c.getLigne();
		this.colonne = c.getColonne();
		this.grille= grille;
		this.panel_transparent = panel_transparent;
		
		// image
		java.net.URL imgUrl2 = getClass().getResource("/images/eau.png");
		this.setIcon(new ImageIcon(new ImageIcon(imgUrl2).getImage().getScaledInstance(LARGEUR_ICONE, HAUTEUR_ICONE, Image.SCALE_DEFAULT)));
		
		// position
		definirPosition();
		
		// quand je clique sur mon extincteur
		this.addMouseListener(new MouseAdapter() {
			@Override
		    public void mousePressed(MouseEvent e){
				// TODO A FAIRE :
				// je mets de l'eau tout autour suivant la matrice 5x5 :
				// <=> parcours de matrice en cercle
				// 50%-----------------------
				// 		90%---------|		|
				// 		|	100%	|		|
				// 		|-----------|		|
				// -------------------------|
				
				// pour l'instant, on �teint tout � deux cases autour
				for(int i=ligne-2 ; i<=ligne+1 ; i++){
					for(int j=colonne-2 ; j<=colonne+1 ; j++){
						grille[i][j].remiseAZero();
					}
				}
				
				// j'enl�ve l'icone extincteur
				panel_transparent.remove(moi);
				panel_transparent.repaint();
		    }
		});
		
		
	}
	
	public void definirPosition(){
		Insets insets = panel_transparent.getInsets();
		Dimension size = this.getPreferredSize();
		int hauteur_case = panel_transparent.getHeight()/grille.length;
		panel_transparent.getSize();
		int largeur_case = panel_transparent.getWidth()/grille[0].length;
		Point p = new Point(this.colonne*largeur_case, this.ligne*hauteur_case);
		this.setBounds(p.x + insets.left - LARGEUR_ICONE/2, p.y + insets.top - HAUTEUR_ICONE/2, size.width, size.height);
	}
	
}
