import me.flickersoul.autosafetree.FileChooserDriver;

import java.io.*;

public class FileReadTest {
    public static void main(String[] args){
        File file = FileChooserDriver.getFile();
        char[] answerArray = null;

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            for(String line; (line = bufferedReader.readLine()) != null; ){
                answerArray = line.toCharArray();
            }

            if(answerArray != null){
                for(char subChar : answerArray){
                    System.out.println((int)subChar + " : " + subChar);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
