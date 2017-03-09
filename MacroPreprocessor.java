package macro.preprocessor;

import java.io.*;
import java.util.*;

/**
 *
 * @author Tejas
 * 
 * Macro Preprocessor:
 * This program processes Macros in the source code and replaces Macro calls by its definitions and arguments.
 * 
 * Tejas Dastane
 * TE B1 1411072
 */

public class MacroPreprocessor {
    
    //############################################## Check these paths before execution ###############################################
    
    //-----------------------------------------------Input files here------------------------------------------------------------------
    static final String FILE_PATH = "E:\\Tejas\\Java Projects\\Macro Preprocessor\\src\\macro\\preprocessor\\";
    static final String INPUT_FILE = FILE_PATH+"Source.txt";
    //---------------------------------------------------------------------------------------------------------------------------------
    //----------------------------------------------Output files here------------------------------------------------------------------
    static final String PASS_1_OUTPUT_FILE = FILE_PATH+"Intermediate Code.txt";
    static final String PASS_2_OUTPUT_FILE = FILE_PATH+"Processed Code.txt";
    //---------------------------------------------------------------------------------------------------------------------------------
    
    //#################################################################################################################################
    
    //----------------------------------------------This contains list of all ALAs-----------------------------------------------------
    static Vector<Vector<ALA>> alas = new Vector<>();
    //---------------------------------------------------------------------------------------------------------------------------------
    
    //----------------------------------------------This is used before any printing statement-----------------------------------------
    static final String ASSEMBLY_PREFIX = "Macro Preprocessor: ";
    //---------------------------------------------------------------------------------------------------------------------------------
    
    //----------------------------------------------Execution times stored here--------------------------------------------------------
    static long totalExecutionTimePass1,totalExecutionTimePass2,totalExecutionTimeAllPasses;
    //---------------------------------------------------------------------------------------------------------------------------------
    
    public static void main(String[] args) throws IOException {
        System.out.println(ASSEMBLY_PREFIX+"Pass 1 started\n");
        System.out.println(ASSEMBLY_PREFIX+"Check the intermediate code generated in "+FILE_PATH+PASS_1_OUTPUT_FILE);
        System.out.println("\n"+ASSEMBLY_PREFIX+"Execution command line outputs for Pass 1:\n");
        pass1();
        System.out.println("...........................................................................................................................\n");
        
        System.out.println(ASSEMBLY_PREFIX+"Printing tables\n");
        System.out.println("Macro Name Table :\n");
        MNT.print();
        System.out.println("\nMacro Definition Table :\n");
        MDT.print();
        System.out.println("...........................................................................................................................\n");
        
        System.out.println(ASSEMBLY_PREFIX+"Pass 2 started\n");
        System.out.println(ASSEMBLY_PREFIX+"Check the output generated in "+FILE_PATH+PASS_2_OUTPUT_FILE);
        System.out.println("\n"+ASSEMBLY_PREFIX+"Execution command line outputs for Pass 2:\n");
        pass2();
        
        totalExecutionTimePass1 = totalExecutionTimePass1/1000000;
        totalExecutionTimePass2 = totalExecutionTimePass2/1000000;
        totalExecutionTimeAllPasses = totalExecutionTimePass1+totalExecutionTimePass2;
        System.out.println(ASSEMBLY_PREFIX+"Preprocessing completed in "+totalExecutionTimeAllPasses+"ms");
    }
    
    //-----------------------------------------------------------Pass 1------------------------------------------------------------------
    private static void pass1() throws FileNotFoundException, IOException{
        
        //---------------------------This Part takes input from the source file ---------------------------------------------------------
        FileReader f = new FileReader(new File(INPUT_FILE));
        BufferedReader b = new BufferedReader(f);
        
        long startTimePass1 = System.nanoTime();
        String line, output_code = "";
        boolean macroStart = false;
        
        //---------------------------This part does the work of updating MNT,MDT and their corresponding ALAs ---------------------------
        while((line=b.readLine())!=null){
            StringTokenizer st = new StringTokenizer(line," ,\t\n",true);
            
            boolean dontPrintNextLine = false;
            while(st.hasMoreTokens()){
                String word = st.nextToken().toUpperCase();
                if(word.equals(" ") || word.equals("\t") || word.equals("\n")) continue;
                
                switch(word){
                    case "MACRO":
                        String macro_defn_line = "";
                        dontPrintNextLine = true;
                        macroStart = true;
                        line = b.readLine();
                        st = new StringTokenizer(line," ,\t\n",true);
                        word = st.nextToken();
                        while(word.equals(" ") || word.equals("\t")) word = st.nextToken();
                        
                        //Get Macro Label if any
                        if(word.startsWith("&")){
                            macro_defn_line += word+" ";
                            new ALA(word);
                            word = st.nextToken();
                            while(word.equals(" ") || word.equals("\t")) word = st.nextToken();
                        }
                        else{
                            new ALA("null");
                        }
                        
                        //Get Macro Name
                        macro_defn_line += word + " ";
                        System.out.println(ASSEMBLY_PREFIX+"------Found Macro "+word+"------");
                        new MNT(word, MDT.getNext_mdt_index_entry());
                        
                        //Get Arguments if any
                        while(st.hasMoreTokens()){
                            word = st.nextToken();
                            while(word.equals(" ") || word.equals("\t")) word = st.nextToken();
                            if(word.startsWith("&")){
                                
                                macro_defn_line += word + " ";
                                if(word.contains("=")){
                                    int equal_to_index = word.indexOf("=");
                                    //String default_arg = word.substring(equal_to_index+1);
                                    word = word.substring(0,equal_to_index);
                                }
                                new ALA(word);
                            }
                            else if(word.equals(",")){
                                macro_defn_line += word + " ";
                            }
                        }
                        
                        //Add Macro definition to MDT
                        new MDT(macro_defn_line);
                        
                        continue;
                    case "MEND":
                        dontPrintNextLine = true;
                        //ALA.print();
                        //Clear ALA and insert MEND into the MDT
                        ALA.print();
                        ALA.deleteALA(alas);
                        macroStart = false;
                        new MDT(word);
                        continue;
                    default:
                        if(!macroStart){
                            output_code += word+" ";
                            continue;
                        }
                        break;
                }
                
                //For processing macro definition
                if(macroStart){
                    dontPrintNextLine = true;
                    String macro_line = "";
                    
                    while(st.hasMoreTokens()){
                        if(word.startsWith("&")){
                            int arg_index = ALA.getIndex(word);
                            macro_line += "#"+arg_index+" ";
                        }
                        else{
                            macro_line += word+" ";
                        }
                        word = st.nextToken();
                    }
                    
                    if(word.startsWith("&")){
                        int arg_index = ALA.getIndex(word);
                        macro_line += "#"+arg_index+" ";
                    }
                    else{
                        macro_line += word+" ";
                    }
                    
                    new MDT(macro_line);
                }
            }
            if(!dontPrintNextLine) output_code += "\n";
        }
        
        //----------------------------------This part handles all the command line outputs and File writing -----------------------------
        System.out.println(ASSEMBLY_PREFIX+"Printing generated Intermediate code: ");
        System.out.println(output_code);
        
        PrintWriter out = new PrintWriter(PASS_1_OUTPUT_FILE);
        for(String output_line: output_code.split("\n")){
            out.println(output_line);
        }
        out.close();
        long endTimePass1 = System.nanoTime();
        totalExecutionTimePass1 = endTimePass1 - startTimePass1;
        System.out.println(ASSEMBLY_PREFIX+"Pass 1 completed in "+totalExecutionTimePass1/1000000+"ms");
    }
    
    
    //-----------------------------------------------------------Pass 2------------------------------------------------------------------
    static void pass2() throws FileNotFoundException, IOException{
        //---------------------------This Part takes input from the source file ---------------------------------------------------------
        FileReader f = new FileReader(new File(PASS_1_OUTPUT_FILE));
        BufferedReader b = new BufferedReader(f);
        
        long startTimePass2 = System.nanoTime();
        String line,output_code="";
        
        //---------------------------This part does the work of updating Macro calls with their definitions -----------------------------
        nextLine:while((line=b.readLine())!=null){
            StringTokenizer st = new StringTokenizer(line," ,=",true);
            
            while(st.hasMoreTokens()){
                String word = st.nextToken();
                int result_macro = MNT.searchMacroNameAndReturnItsMDTIndex(word);
                
                //If the word is not a macro call
                if(result_macro == -1){
                    output_code += word+" ";
                }
                else{
                    String mdt_code = MDT.getMacroCode(result_macro, line);
                    output_code += mdt_code;
                    continue nextLine;
                }
            }
            output_code += "\n";
        }
        
        //----------------------------------This part handles all the command line outputs and File writing -----------------------------
        System.out.println(ASSEMBLY_PREFIX+"Printing generated Output Code: ");
        System.out.println(output_code);
        
        PrintWriter out = new PrintWriter(PASS_2_OUTPUT_FILE);
        for(String output_line: output_code.split("\n")){
            out.println(output_line);
        }
        out.close();
        long endTimePass2 = System.nanoTime();
        totalExecutionTimePass2 = endTimePass2 - startTimePass2;
        System.out.println(ASSEMBLY_PREFIX+"Pass 2 completed in "+totalExecutionTimePass2/1000000+"ms");
    }
    
}



//-----------------------------------------------This module will handle all the updations in MNT-----------------------------------------
class MNT{
    private int index;
    private static int ids=0;
    private String name;
    private int mdt_index;
    private static Vector<MNT> mnt = new Vector<>();

    public MNT(String name, int mdt_index) {
        index = ids++;
        this.name = name;
        this.mdt_index = mdt_index;
        mnt.add(this);
    }
    
    public static void print(){
        System.out.println("Index\tMacro Name\tMDT index");
        for(MNT m: mnt){
            System.out.println(m.index+"\t"+m.name+"\t\t"+m.mdt_index);
        }
    }
    
    public static int searchMacroNameAndReturnItsMDTIndex(String word){
        int index_in_mdt=-1;
        for(int i=0;i<mnt.size();i++){
            String mac_name = mnt.get(i).name;
            if(mac_name.equals(word)){
                index_in_mdt = i;
                break;
            }
        }
        if(index_in_mdt == -1) return -1;
        index_in_mdt = mnt.get(index_in_mdt).mdt_index;
        return index_in_mdt;
    }
}

//-----------------------------------------------This module will handle all the updations in MDT-----------------------------------------
class MDT{
    private int index;
    private String macro_defn_line;
    private static int next_mdt_index_entry=0;
    private static Vector<MDT> mdt = new Vector<>();
    
    public MDT(String macro_defn_line) {
        this.index = next_mdt_index_entry++;
        this.macro_defn_line = macro_defn_line;
        mdt.add(this);
    }

    public static int getNext_mdt_index_entry() {
        return next_mdt_index_entry;
    }
    
    public static void print(){
        System.out.println("Index\tMacro Definition");
        for(MDT m: mdt){
            System.out.println(m.index+"\t"+m.macro_defn_line);
        }
    }
    
    /**This is the most important function upon which the whole Pass 2 is dependent!!! 
     * This will return the macro code, replacing all the argument ids from Pass 1,
     * by taking input the start of macro in MDT and the macro call line.
     */
    public static String getMacroCode(int mdt_index, String macro_call_line){
        String macro_code="";
        String macro_name="";
        StringTokenizer st = new StringTokenizer(macro_call_line," ,\t",false);

        String word = st.nextToken();
        boolean isContainingLabel = false;
        boolean isMacroNameSkipped = false,firstWord = true;

        /**
         * This Part makes a list of all arguments used in Macro definition along with default values if any.
         */
        String mdt_defn_line = mdt.get(mdt_index).macro_defn_line;
        StringTokenizer st2 = new StringTokenizer(mdt_defn_line," ",false);


        Vector<String[]> a = new Vector<>();
        Vector<ALA> ala = new Vector<>();
        int i=0;
        while(st2.hasMoreTokens()){
            String mdt_word = st2.nextToken();
            
            if(macro_name.equals("")) macro_name=mdt_word;
            if(firstWord){
                if(mdt_word.startsWith("&")){
                    isContainingLabel = true;
                    macro_name="";
                }
                firstWord=false;
            }
            if(mdt_word.contains("=")){
                //Default arguments specified

                int in = mdt_word.indexOf("=");
                String formal_arg = mdt_word.substring(0,in);
                String def_val = mdt_word.substring(in+1,mdt_word.length());
                a.add(new String[]{i++ + "",formal_arg,def_val});
                ala.add(new ALA(formal_arg));
            }
            else if(mdt_word.contains("&")){
                //Only argument
                if(!isContainingLabel && !isMacroNameSkipped){
                    a.add(new String[]{i++ + "","&label","null"});
                    ala.add(new ALA("null"));
                    isMacroNameSkipped = true;
                }
                a.add(new String[]{i++ + "",mdt_word,"null"});
                ala.add(new ALA(mdt_word));
                if(isContainingLabel && !isMacroNameSkipped){
                    mdt_word = st2.nextToken();
                    macro_name=mdt_word;
                    isMacroNameSkipped = true;
                }
            }
        }



        /**
         * This Part will enter the arguments passed in macro call into the ALA.
         */
        i=0;
        isMacroNameSkipped = false;
        while(true){
            if(word.contains("=")){
                //Formal and actual both present

                int in = word.indexOf("=");
                String formal_arg = word.substring(0,in);
                String actual_arg = word.substring(in+1,word.length());

                in = ALA.getIndex(formal_arg);
                if(in==-1){
                    throw new IllegalArgumentException("Gadbad kar diya");
                }
                ala.get(in).update(in, actual_arg);
                i++;
                if(st.hasMoreTokens()) word = st.nextToken();
                else break;
            }
            else{

                //If-else to handle skipping macro name
                if(!isMacroNameSkipped && !isContainingLabel){
                    word = st.nextToken();
                    i++;
                    isMacroNameSkipped = true;
                    continue;
                }
                else if(!isMacroNameSkipped){
                    ala.get(0).update(0,word);
                    i++;
                    word = st.nextToken();
                    word = st.nextToken();
                    isMacroNameSkipped = true;
                    continue;
                }

                ala.get(i).update(i++, word);
                if(st.hasMoreTokens()) word = st.nextToken();
                else break;
            }
        }

        /**Enter default arguments if macro arguments do not match total arguments as defined in the definition.
         * 
         * This is done by comparing the list compiled before which contains the arguments defined in macro definition
         * along with its default values.
         * 
         */
        if(a.size() != i){
            i = ala.size()-1;
            while(i<a.size()){
                String[] argvals = a.get(i++);
                ala.get(i-1).update(i-1, argvals[2]);
            }
        }

        /**
         * This part processes the Macro definition and replaces all the argument ids by the actual arguments specified in
         * macro call or by default arguments if needed.
         */
        mdt_index++;
        String macro_line = mdt.get(mdt_index++).macro_defn_line;
        do{
            st2 = new StringTokenizer(macro_line," ,",true);
            while(st2.hasMoreTokens()){
                String macro_word = st2.nextToken();
                if(macro_word.startsWith("#")){
                    macro_word = macro_word.substring(1);
                    macro_code += ALA.getArg(Integer.parseInt(macro_word));
                }
                else{
                    macro_code += macro_word;
                }
            }
            macro_line = mdt.get(mdt_index++).macro_defn_line;
            macro_code += "\n";
        }
        while(!macro_line.trim().equals("MEND"));
        System.out.println(MacroPreprocessor.ASSEMBLY_PREFIX+"Processing Macro "+macro_name);
        ALA.print();
        ALA.deleteALA(MacroPreprocessor.alas);
        return macro_code;
    }
}

//-----------------------------------------------This module will handle all the updations in ALA-----------------------------------------
class ALA{
    private int index;
    private String arg;
    private static Vector<ALA> ala = new Vector<>();
    private static int ala_entry_id=0;

    public ALA(String arg) {
        this.index = ala_entry_id++;
        this.arg = arg;
        ala.add(this);
    }

    public static void update(int in,String arg){
        ala.get(in).arg = arg;
    }
    
    public static int getIndex(String macro_arg) {
        int in=-1;
        for(int i=0;i<ala.size();i++){
            if(ala.get(i).arg.equals(macro_arg)) in=i;
        }
        return in;
    }
    
    public static void deleteALA(Vector<Vector<ALA>> v){
        v.add(ala);
        ala.removeAllElements();
        ala_entry_id=0;
    }
    
    public static void print(){
        System.out.println("ALA:\n\nIndex\tArgs");
        for(ALA a: ala){
            System.out.println(a.index+"\t"+a.arg);
        }
        System.out.println("");
    }

    public static String getArg(int in) {
        return ala.get(in).arg;
    }
}