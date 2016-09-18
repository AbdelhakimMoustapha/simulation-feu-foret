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
 * FICHIER : Vent.java
 * 
 * l'objet Vent nous permet de g�n�rer une matrice de probabilit�s selon deux param�tres :
 * . intensit�
 * . rotation
 * 
 * jpanel pour �tre visible par dessus la Carte
 * 
 * il est recommand� de lire le rapport pour visualiser le d�grad� radial du vent
 * 
 */

package entites;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import javax.swing.JPanel;

import outils.Matrice;

public class Vent extends JPanel implements Serializable {
	private static final long serialVersionUID = 387781645905175290L;
	
	
	private int intensite = 20;
	@SuppressWarnings("unused")
	private double largeur = 10;
	private int direction;
	
	private boolean firstTime = true;
	
	private Matrice matrice;
	
	transient private BufferedImage bi;
	
	private Carte carte;
	
	private boolean showMouseColor = false;
	
	private Point2D centreVent;
	
	public Vent(Carte carte){
		super();
		this.setOpaque(false);
		this.carte = carte;
		this.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent me) {
				if(showMouseColor){
					int rgb = bi.getRGB(me.getX(), me.getY());
					int alpha = (rgb >> 24) & 0x000000FF;
					int red = (rgb >> 16) & 0x000000FF;
					int green = (rgb >>8 ) & 0x000000FF;
					int blue = (rgb) & 0x000000FF;
//					try {
//			            ImageIO.write(bi, "png", new File("test.png"));
//			        } catch (IOException e) {
//			            e.printStackTrace();
//			        }
					System.out.println("Red:"+red+" | Green:"+green+" | Blue:"+blue+" | Alpha:"+alpha);
				}
			}
			
			@Override
			public void mouseDragged(MouseEvent arg0) {
			}
		});
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
	public void genererMatrice(){	
		if(bi == null){
//			System.out.println("bi NULL dans get matrice");
			return;
		}
		
		
		// pour chaque case, je prends son centre
		// sa probabilit� de s'enflammer
		// sera son pourcentage de blanc
		// si 60% de blanc, elle aura 60% de chance de s'enflammer
		
		Case[][] grille = carte.getGrille();
		
		int hauteur = grille.length;
		int largeur = grille[0].length;
		
		
		
		
		int[][] matrice = new int[hauteur][largeur];
		
		
		for(int i=0 ; i<hauteur ; i++){
			for(int j=0 ; j<largeur ; j++){
				// on r�cup�re le centre en XYZ du jlabel
				int demiLargeurCase = grille[i][j].getWidth() / 2;
				int demiHauteurCase = grille[i][j].getHeight() / 2;
				int abscisse = grille[i][j].getX() + demiLargeurCase;
				int ordonnee = grille[i][j].getY() + demiHauteurCase;

				int point = bi.getRGB(abscisse, ordonnee);
				
				// r�cup�ration de sa valeur de bleu (0-255)
				int alpha = (point >> 24) & 0x000000FF;
				int red = (point >> 16) & 0x000000FF;
				int green = (point >>8 ) & 0x000000FF;
				int blue = (point) & 0x000000FF;
				
				int pourcentageDeBleu = 0;
				
				// correction d'un petit bug, si on fait tourner le vent, on fait tourner l'image
				// du coup certains pixels seront en dehors de la zone
				// pourquoi rouge, bleu et vert == 238 ? Aucune id�e..
				// on les �limine comme suit :
				if(alpha == 255 && red == 238 && blue == 238 && green == 238){
					pourcentageDeBleu = 0;
				} else {
					// conversion en pourcentage
					pourcentageDeBleu = blue * 100 / 255;
				}

				
				
				// ajout dans la matrice de pourcentage
				matrice[i][j] = pourcentageDeBleu;
			}
		}
		
		// un point est � noter cependant :
		// il faut d�tecter quel est le centre du vent,
		// ce centre qui sera utilis� comme case de r�f�rence
		// on lui donnera une probabilit� de 101  
		// on va le chercher :
		int colonneCentre = (int)(centreVent.getX()/getWidth()*largeur);
		int ligneCentre = (int)(centreVent.getY()/getHeight()*hauteur);
		
		matrice[ligneCentre][colonneCentre] = 101;
		
				
		this.matrice = new Matrice(matrice);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // diff�rentes options pour la visualisation du vent
        // ils ne sont modififables que depuis le code
        // boolean parametre = valeurActuelle; // valeurRecommand�e
        boolean voirDegrade = false; 				// false
        boolean voirCentreDegrade = false;			// false
        boolean voirCentreVent = false;				// true
        boolean voirFocus = false;					// false
        boolean voirMatriceDeProba = true;			// true
        boolean voirLesZerosDeMatriceProba = false;	// false
        
        Graphics2D g2 = (Graphics2D)g;
        int width = this.getWidth();
        int height = this.getHeight();
        
        // si vous voulez voir le d�grad� radial, mettez le � vrai
        // ATTENTION : le vent marchera mais ne sera pas impact� par les modifications (direction, intensit�)
        // car la bufferedImage ne sera plus actualis�e
        // DONC pour que tout marche, on mets le bool�en � faux
        // allez savoir pourquoi le d�grad� n'apparait plus quand la bufferedimage est actualis�e...        
        if(voirDegrade){
	        if(bi == null){
			    bi = (BufferedImage)(this.createImage(width,height));
		    	g2 = bi.createGraphics();
	        }
        } else {
        	// ici se trouve la bonne solution
        	bi = (BufferedImage)(this.createImage(width,height));
	    	g2 = bi.createGraphics();
        }

        // le gradient n'est qu'une couleur de fond
        // elle n'existe pas en tant que telle
        // il faut cr�er une forme puis la remplir d'un d�grad�
        // ici la forme est le panel : on cr�e un rectangle de la taille du panel
        Rectangle r = new Rectangle(0, 0, width, height);
        /// cr�ation du d�grad�
        // RAYON
        float rayon = intensite*2;
        // CENTRE DEGRADE
        Point2D centre = new Point2D.Float(width/2+rayon, height/2);
        // CENTRE VENT
        centreVent = new Point2D.Float((int)centre.getX() - rayon , (int)centre.getY());
        
        // FOCUS
        Point2D focus = new Point2D.Float(0, (int)centre.getY());
        float[] dist = {0.0f, 1.0f};
        Color couleurCentre	= new Color(0, 0, 0, 255);
        Color couleurLoin	= new Color(255, 255, 255, 255);
        Color[] colors = {couleurLoin, couleurCentre};
        RadialGradientPaint rgp =
                new RadialGradientPaint(centre, rayon, focus,
                                        dist, colors,
                                        CycleMethod.NO_CYCLE);
        g2.setPaint(rgp);
        double angleRotation = Math.toRadians(-direction);
        // largeur
//        g2.scale(largeur*10,largeur*10);
        g2.rotate(angleRotation, centreVent.getX(), centreVent.getY());
        g2.fill(r);
        
        
        
        // g�n�ration de la matrice de probabilit�s
        genererMatrice();
        
        
        // montrer point de focus
        if(voirFocus){
	        g.setColor(Color.green);
	        g.fillOval((int)focus.getX(), (int)focus.getY(), 15, 15);
        }
        
        // montrer centre
        if(voirCentreDegrade){
	        g.setColor(Color.magenta);
	        g.fillOval((int)centre.getX(), (int)centre.getY(), 15, 15);
        }
        
        // montrer notre centre � nous : le centre du vent
        if(voirCentreVent){
        	g.setColor(Color.blue);
            g.fillOval((int)centreVent.getX(), (int)centreVent.getY(), 15, 15);	
        }
        
        // affichage pour test des centres des jlabel avec des points rouges
        if(voirMatriceDeProba){
	        Case[][] grille = carte.getGrille();
	        for(int i=0 ; i<grille.length ; i++){
				for(int j=0 ; j<grille[0].length ; j++){
					// on r�cup�re le centre en XYZ du jlabel
					int abscisse = grille[i][j].getX() + (grille[i][j].getWidth() / 2);
					int ordonnee = grille[i][j].getY() + (grille[i][j].getHeight() / 2);
					
					int red = matrice.getValeur(i, j);
					
					if(voirLesZerosDeMatriceProba || red != 0){
						g.setColor(new Color(red,0,0,255));
						if(red == 101){
							g.setColor(Color.orange);						
						}
						int rayonPoint = 10;
				        g.fillOval(abscisse-rayonPoint/2, ordonnee-rayonPoint/2, rayonPoint, rayonPoint);
				        g.drawString(Integer.toString(red), abscisse, ordonnee);
					}
				}
			}
        }
        
		// on la crop
		matrice = matrice.crop();
		
		// affichage
//		System.out.println("Affichage matrice crop�e du vent dans Vent :");
//		System.out.println(matrice);
		
		if(firstTime){
			firstTime = false;
			this.setVisible(false);
		}
	}
        
	/**
	 * toutes les probabilit�s, m�me celles de la prof
	 * ont un centre, c'est la case actuelle, "relative"
	 * par rapport � laquelle la probabilit� de toutes les cases autour d�coule
	 * 
	 * dans le mod�le de la prof, tout est �crit en dur, m�me le centre
	 * 
	 * Pas chez nous ! Il faut donc le trouver, c'est facile
	 * c'est le seul qui a un pourcentage sup�rieur � 100 (nous avons choisi 101)
	 * 
	 * @return
	 */
	public Point trouverCentre(){
		for(int i=0 ; i<matrice.getHauteur() ; i++){
			for(int j=0 ; j<matrice.getLargeur() ; j++){
				if(matrice.getValeur(i, j) > 100){
					return new Point(j, i);
				}
			}
		}
		return null;
	}
        
	
	
	public Matrice getMatrice(){
		return matrice;
	}
	
	
	

	public int getIntensite() {
		return intensite;
	}

	public void setIntensite(int intensite) {
		this.intensite = intensite;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public void setLargeur(double largeur) {
		this.largeur = largeur;		
	}
	
	public BufferedImage getBufferedImage(){
		return bi;
	}
}
