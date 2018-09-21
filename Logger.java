
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Ikeda-Lab15
 */
public class Logger {
    int LogLevel = 0;
    StringBuilder logstr;

    public Logger(int level){
        this.LogLevel = level;
        this.logstr = new StringBuilder();
    }

    // logへの追加
    public void logappended(Info info){
        // 数字が大きくなるほど，logに追加する情報が増える   
        
        if(this.LogLevel <= 0){
            //
        }
        if(this.LogLevel <= 10){
            // 
        }
    }
    
    
    
    // ファイルにログを出力
    public void OutputFileLog(String name){
        try {
            File file = new File(name);

            if (checkBeforeWritefile(file)) {
                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
                pw.print(logstr);
                pw.close();
            } else {
                System.out.println("ファイルに書き込めません");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    
    public void OutputFileLog(String name, String str){
        try {
            File file = new File(name);

            if (checkBeforeWritefile(file)) {
                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
                pw.print(str);
                pw.close();
            } else {
                System.out.println("ファイルに書き込めません");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    
    public void OutputFileLog(String name, String str, boolean tf){
        try {
            File file = new File(name);

            if (checkBeforeWritefile(file)) {
                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, tf)));
                pw.print(str);
                pw.close();
            } else {
                System.out.println("ファイルに書き込めません");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    
    public boolean checkBeforeWritefile(File file) {
        if (file.exists()) {
            if (file.isFile() && file.canWrite()) {
                return true;
            }
        }
        else{
            try{
                file.createNewFile();
                return true;
            }catch(IOException e){
                System.out.println(e);
            }
        }
        
        return false;
    }
    
    
    // logを得る
    public String getLog(){
        return new String(this.logstr);
    }
    
    // lebel,logの初期化
    public void initLog(int level){
        this.LogLevel = level;
        this.logstr = new StringBuilder();
    }
}
