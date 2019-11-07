package file_io;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CSVFile extends AsyncTask<Integer, Void, Void> {

    private Resources resources;
    private ArrayList<String> csvList;

    public CSVFile(Resources resources, ArrayList<String> staticList) {
        this.resources = resources;
        this.csvList = staticList;
    }

    @Override
    protected Void doInBackground(Integer... integers) {
        Integer csv_id = integers[0];

        InputStream inputStream = resources.openRawResource(csv_id);
        String csvLine;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            while ((csvLine = br.readLine()) != null) {
                this.csvList.add(csvLine);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException("Error in reading CSV file: " + e);
        }
        finally {
            try {
                inputStream.close();
            }
            catch (IOException e) {
                throw new RuntimeException("Error while closing input stream: " + e);
            }
        }
        Log.i("FINISH", "READING CSV");
        return null;
    }
}
