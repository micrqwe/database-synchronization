package io.github.micrqwe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.micrqwe.model.SyncCallback;
import io.github.micrqwe.model.TableKey;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Observable;
import java.util.Observer;

/**
 * @author shaowenxing@cnstrong.cn
 * @since 14:00
 */
public class DatabaseService implements Observer {
    private ObjectMapper objectMapper = new ObjectMapper();

    private String file;

    @Override
    public void update(Observable o, Object arg) {
        SyncCallback syncCallback = new SyncCallback();
        try {
            TableKey tableKey = new TableKey();
            tableKey.setTable(syncCallback.getTable());
            tableKey.setId(syncCallback.getId());
            String s = objectMapper.writeValueAsString(tableKey);
            File file = new File(this.file);
            OutputStream os = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(os);
            pw.println(s);//每输入一个数据，自动换行，便于我们每一行每一行地进行读取
            pw.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
