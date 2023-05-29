import java.io.*;

/*
  NumParser.java
  
  Beispiel zur Vorlesung
  
  Realisiert die folgende kontextfreie Grammatik f�r Strings aus Ziffern
  num -> digit num | num
  digit -> '1'|'2'|'3'|'4'|'5'|'6'|'7'|'8'|'9'|'0'
  
  Der Parser ist nach dem Prinzip des rekursiven Abstiegs programmiert,
  d.h. jedes nicht terminale Symbol der Grammatik wird durch eine 
  Methode in Java repr�sentiert, die die jeweils anderen nicht terminalen
  Symbole auf der rechten Seite der Grammatik Regeln ggf. auch rekursiv
  aufruft.
  
  Der zu parsende Ausdruck wird aus einer Datei gelesen und in einem
  Array of Char abgespeichert. Pointer zeigt beim Parsen auf den aktuellen
  Eingabewert.
  
  Ist der zu parsende Ausdruck syntaktisch nicht korrekt, so werden 
  �ber die Methode syntaxError() entsprechende Fehlermeldungen ausgegeben

*/

public class NumParser{
    // Konstante f�r Ende der Eingabe

    // Anfang Attribute
    static final char EOF=(char)255;
    // Zeiger auf das aktuelle Eingabezeichen
    static int pointer = 0;
    // Zeiger auf das Ende der Eingabe
    static int maxPointer = 0;
    // Eingabe zeichenweise abgelegt
    static char input[];
    // Ende Attribute


    //-------------------------------------------------------------------------
    //-------------------Methoden der Grammatik--------------------------------
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    // num -> digit num | digit
    // Eine Zahl ist eine Ziffer gefolgt von einer Zahl oder nur eine einzelne Ziffer
    // Der Parameter t gibt die aktuelle Rekursionstiefe an
    //-------------------------------------------------------------------------


    // Anfang Methoden
    static boolean num(int t){
        char [] digitSet = {'1','2','3','4','5','6','7','8','9','0'};
        char [] eof = {EOF};
        ausgabe("num->", t);          //Syntaxbaum ausgeben
        if (lookAhead(digitSet))
            return digit(t+1)&& num(t+1);   //num->digit num
        else if (lookAhead(eof))
            return true; //num->epsilon
        else
            return digit(t+1);          //num->digit
    }//num

    //-------------------------------------------------------------------------
    // term -> operator rightTerm
    // Ein Term ist ein Operator gefolgt von einem rightTerm
    // Der Parameter t gibt die aktuelle Rekursionstiefe an
    //-------------------------------------------------------------------------
    static boolean term(int t){
        ausgabe("term->", t);
        return operator(t+1) && rightTerm(t+1);
    }

    //-------------------------------------------------------------------------
    // operator -> '(' expression ')' | num
    // Ein Operator ist entweder eine Zahl oder ein Ausdruck in Klammern
    // Der Parameter t gibt die aktuelle Rekursionstiefe an
    //-------------------------------------------------------------------------
    static boolean operator(int t) {
        ausgabe("operator->", t);
        char[] leftBracket = {'('};
        char[] rightBracket = {')'};

        if (match(leftBracket, t + 1)) { // If a left bracket is found, then the expression is parsed recursively
            if (expression(t + 1)) {
                if (match(rightBracket, t + 1)) { // If a right bracket is found, then the operand is parsed recursively
                    return true; // operand -> (expression)
                } else {
                    syntaxError(") erwartet"); // Error if no right bracket is found
                    return false;
                }
            } else {
                syntaxError("Ausdruck erwartet"); // Error if no expression is found
                return false; // operand -> (expression)
            }
        } else {
            return num(t + 1);
        }
    }

    //-------------------------------------------------------------------------
    // expression -> term rightExpression
    // Ein Ausdruck ist ein Term gefolgt von einem rechten Ausdruck
    // Der Parameter t gibt die aktuelle Rekursionstiefe an
    //-------------------------------------------------------------------------
    static boolean expression(int t) {
        ausgabe("expression->", t); // Syntaxbaum ausgeben
        if (term(t + 1)) {
            return rightExpression(t + 1);
        } else {
            syntaxError("Term erwartet"); // Fehler, wenn kein Term gefunden wurde
            return false;
        }
    }

    //-------------------------------------------------------------------------
    // rightTerm -> '*' operand rightTerm | '/' operand rightTerm | epsilon
    //
    // Der Parameter t gibt die aktuelle Rekursionstiefe an
    //-------------------------------------------------------------------------
    static boolean rightTerm(int t) {
        ausgabe("rightTerm->", t);
        char[] operatorSet = {'*','/'}; // Set of operators
        // If a operator is found, then the rightTerm is parsed recursively
        if (match(operatorSet, t + 1)) {
            return operator(t + 1) && rightTerm(t + 1);
        } else {
            ausgabe("Rechter Term ist Leer", t); // rightTerm -> epsilon
            return true;
        }
    }


    //-------------------------------------------------------------------------
    // expression → term rightExpression
    //
    // Der Parameter t gibt die aktuelle Rekursionstiefe an
    //-------------------------------------------------------------------------
    static boolean rightExpression(int t) {
        char[] matchSet = {'+', '-'};
        ausgabe("rightExpression->", t);          //Syntaxbaum ausgeben
        if (match(matchSet, t + 1)) {
            return term(t+1) && rightExpression(t + 1); // rightExpression -> +/- term rightExpression
        } else {
            return true; // rightExpression -> epsilon
        }
    }


    //-------------------------------------------------------------------------
    // digit -> '1'|'2'|'3'|'4'|'5'|'6'|'7'|'8'|'9'|'0'
    // Eine Digit ist ein Terminalsymbol, d.h. ein Zeichen aus dem Alphabet
    // Der Parameter t gibt die aktuelle Rekursionstiefe an
    //-------------------------------------------------------------------------
    static boolean digit(int t){
        char [] matchSet = {'1','2','3','4','5','6','7','8','9','0'};
        ausgabe("digit->",t);     //Syntaxbaum ausgeben
        if (match(matchSet,t+1)){   //digit->'1'|'2'...|'9'|'0'
            return true;                // korrekte Ableitung der Regel m�glich
        }else{
            syntaxError("Ziffer erwartet"); // korrekte Ableitung der Regel
            return false;                   // nicht m�glich
        }
    }//digit







    //-------------------------------------------------------------------------
    //-------------------Hilfsmethoden-----------------------------------------
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    // Methode, die testet, ob das aktuele Eingabezeichen unter den Zeichen
    // ist, die als Parameter (matchSet) �bergeben wurden.
    // Ist das der Fall, so gibt match() true zur�ck und setzt den Eingabe-
    // zeiger auf das n�chste Zeichen, sonst wird false zur�ckgegeben und der
    // Eingabezeiger bleibt unver�ndert.
    //
    // Der Parameter matchSet �bergibt die zu pr�fenden Eingabezeichen
    // Der Parameter t gibt die aktuelle Rekursionstiefe an
    //-------------------------------------------------------------------------

    static boolean match(char [] matchSet,int t){
        for (int i=0;i<matchSet.length;i++)
            if (input[pointer]==matchSet[i]){
                ausgabe(" match: "+input[pointer],t);
                pointer++;  //Eingabepointer auf das n�chste Zeichen setzen
                return true;
            }
        return false;
    }//match



    //-------------------------------------------------------------------------
    //Methode, die testet, ob das auf das aktuelle Zeichen folgende Zeichen
    //unter den Zeichen ist, die als Parameter (aheadSet) �bergeben wurden.
    //Der Eingabepointer wird nicht ver�ndert!
    //
    // Der Parameter aheadSet �bergibt die zu pr�fenden Lookahead-Zeichen
    //-------------------------------------------------------------------------
    static boolean lookAhead(char [] aheadSet){
        for (int i=0;i<aheadSet.length;i++)
            if (input[pointer+1]==aheadSet[i])
                return true;
        return false;
    }//lookAhead



    //-------------------------------------------------------------------------
    // Methode zum zeichenweise Einlesen der Eingabes aus
    // einer Eingabedatei.
    // Die Metode ber�cksichtigt beim Einlesen schon die maximale Gr�sse
    // des Arrays input von 256 Zeichen.
    // Das Ende der Eingabe wird mit EOF markiert
    //
    // Der Parameter name enth�lt den Dateinamen
    //-------------------------------------------------------------------------

    static boolean readInput(String name){
        int c=0;
        try{
            FileReader f = new FileReader(name);
            for(int i=0;i<256;i++){
                c = f.read();
                if (c== -1){
                    maxPointer=i;
                    input[i]=EOF;
                    break;
                }else
                    input[i]=(char)c;
            }
        }
        catch(Exception e){
            System.out.println("Fehler beim Dateizugriff: "+name);
            return false;
        }
        return true;
    }//readInput






    //-------------------------------------------------------------------------
    // Methode, die testet, ob das Ende der Eingabe erreicht ist
    // (pointer == maxPointer)
    //-------------------------------------------------------------------------

    static boolean inputEmpty(){
        if (pointer==maxPointer){
            ausgabe("Eingabe leer!",0);
            return true;
        }else{
            syntaxError("Eingabe bei Ende des Parserdurchlaufs nicht leer");
            return false;
        }
    }//inputEmpty



    //-------------------------------------------------------------------------
    // Methode zum korrekt einger�ckten Ausgeben des Syntaxbaumes auf der
    // Konsole
    //
    // Der Parameter s �bergibt die Beschreibung des Knotens als String
    // Der Parameter t �bergibt die Einr�ck-Tiefe
    //-------------------------------------------------------------------------

    static void ausgabe(String s, int t){
        for(int i=0;i<t;i++)
            System.out.print("  ");
        System.out.println(s);
    }//ausgabe



    //-------------------------------------------------------------------------
    // Methode zum Ausgeben eines Syntaxfehlers mit Angabe des vermuteten
    // Zeichens, bei dem der Fehler gefunden wurde
    //
    // Der Parameter s �ebrgibt die Fehlermeldung als String
    //-------------------------------------------------------------------------

    static void syntaxError(String s){
        System.out.println("Syntax Fehler beim "+(pointer+1)+" Zeichen: "
                +input[pointer]);
        System.out.println(s);
    }//syntaxError



    //-------------------------------------------------------------------------
    // Main Methode, startet den Parser und gibt das ERgebnis des Parser-
    // durchlaufs auf der Konsole aus
    //-------------------------------------------------------------------------

    public static void main(String args[]){
        // Anlegen des Arrays f�r den zu parsenden Ausdruck
        input = new char[256];

        // Einlesen der Datei und Aufruf des Parsers
        if (readInput("testdatei.txt"))
            if (expression(0)&& inputEmpty())
                System.out.println("Korrekter Ausdruck");
            else
                System.out.println("Fehler im Ausdruck");
    }//main
    // Ende Methoden
}