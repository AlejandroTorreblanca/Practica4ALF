package c�digo;

import java.io.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FIEBDCFixing {

	private static final String registro="^~([ABCDEFGKLMNOPQRTWXY])(|.*|)";
	private static final String registroV="^~V(|.*|)";
	
	/**
	 * Comprueba que la extensi�n del nombre del fichero sea la correcta.
	 * @param nombreFichero nombre que se desea comprobar.
	 * @return true si tiene extensi�n .stl, false en caso contrario.
	 */
	public static boolean comprobarNombreFichero(String nombreFichero) 
	{
       Pattern pat = Pattern.compile("\\.[BC3]|[bc3]$");
       Matcher mat = pat.matcher(nombreFichero);
       if (mat.find()) //En este caso si que se puede usar find
    	   return true; 	//Caso en el que el fichero tiene extensi�n correcta
        else 
    	   return false;    //Caso en el que el fichero no tiene extensi�n correcta
	}
	
	private static void procesarRegistroV(String linea) {
		Pattern pat = Pattern.compile(registroV);
	    Matcher mat = pat.matcher(linea);
	    System.out.println("Linea: " + linea);
	    if (!mat.matches())
	    	errorDeLectura1();
	    String cadena=mat.group(1);
	    System.out.println(mat.group(1));
	    Pattern pat2 = Pattern.compile("\\|(.*)\\|");
	    Matcher mat2 = pat2.matcher(cadena);
	    while (mat2.find())
	    {
	    	System.out.println("grupo: "+mat2.group());
	    	
	    }
	    
	}
	
	private static void procesarLinea(String linea) {
		Pattern pat = Pattern.compile(registro);
	    Matcher mat = pat.matcher(linea);
	    if (!mat.matches())
	    	errorDeLectura1();
	    String cabecera=mat.group(1);
	    //String cadena=mat.group(2);
	    switch (cabecera) {
		case "C":
			
			break;
		case "T":
	
			break;

		default:
			break;
		}
		
	}
	
	public static void errorDeLectura1()
	{
		System.err.println("Error en la lectura del fichero, el fichero est� incompleto.\nPrograma finalizado.");
		System.exit(0);
	}
	
	public static void leerFichero(String nombreFichero)throws IOException 
	{
		File ficheroLectura = new File (nombreFichero); // Crea un objeto File a partir del nombre del fichero a leer
		String nombreNuevo=nombreFichero.concat("Fixed.bc3");
		System.out.println(nombreNuevo);
		File ficheroEscritura = new File (nombreNuevo);
		PrintWriter escritor = new PrintWriter(ficheroEscritura);
		FileReader fr = new FileReader(ficheroLectura); //Crea un FileReader para recorrer el fichero y recuperar el contenido
		BufferedReader br = new BufferedReader(fr); //Crea un BufferedReader a partir del FileReader para leer por l�neas el contenido.
		String linea;
		boolean finDeFichero=false;
		if( (linea = br.readLine()) == null )
			finDeFichero=true;
		else
		{
			procesarRegistroV(linea);
		}
		/*while(!finDeFichero)
		{
			if( (linea = br.readLine()) == null )
				finDeFichero=true;
			else
				procesarLinea(linea);
		}*/
		br.close();
		fr.close();
		escritor.close();
	}
	
	public static void main(String[] args) {
		Scanner consola = new Scanner(System.in); //Crea Scanner para leer por consola
		boolean control=true;
		while(control)
		{
			System.out.println("Nombre del fichero a leer con extensi�n .bc3 o .BC3: ");
			String nombreFichero=consola.nextLine(); //lee la l�nea con el nombre de fichero  desde consola
		    if(!comprobarNombreFichero(nombreFichero))
				System.err.println("Extensi�n del fichero incorrecta, asegurese de que tiene extensi�n .bc3 o .BC3.");
			else 
			{
				try {
					leerFichero(nombreFichero);	//se intenta leer el contenido de fichero
					control=false;			  
				} catch (IOException e1) {
					System.err.println("***Error de lectura***\n Asegurese de que el fichero existe."); 
					control=true;
				    } 	
			}
		}
		
		consola.close();
	}

}
