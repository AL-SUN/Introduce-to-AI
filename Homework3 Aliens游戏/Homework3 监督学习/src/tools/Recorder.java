/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import core.game.Observation;
import core.game.StateObservation;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedList;

import ontology.Types;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

/**
 *
 * @author yuy
 */
public class Recorder {
    public FileWriter filewriter;
    public static Instances s_datasetHeader = datasetHeader();
    
    public Recorder(String filename) throws Exception{
        
        filewriter = new FileWriter(filename+".arff");
        filewriter.write(s_datasetHeader.toString());
        /*
                // ARFF File header
        filewriter.write("@RELATION AliensData\n");
        // Each row denotes the feature attribute
        // In this demo, the features have four dimensions.
        filewriter.write("@ATTRIBUTE gameScore  NUMERIC\n");
        filewriter.write("@ATTRIBUTE avatarSpeed  NUMERIC\n");
        filewriter.write("@ATTRIBUTE avatarHealthPoints NUMERIC\n");
        filewriter.write("@ATTRIBUTE avatarType NUMERIC\n");
        // objects
        for(int y=0; y<14; y++)
            for(int x=0; x<32; x++)
                filewriter.write("@ATTRIBUTE object_at_position_x=" + x + "_y=" + y + " NUMERIC\n");
        // The last row of the ARFF header stands for the classes
        filewriter.write("@ATTRIBUTE Class {0,1,2}\n");
        // The data will recorded in the following.
        filewriter.write("@Data\n");*/
        
    }

    /**
     * Feature:- information of every position on the image: itype of object on every position(32*14)
     *         - 4 states feature:GameTick,AvatarSpeed,AvatarHealthPoints,AvatarType
     * @param obs state to extract feature
     * @return feature list[453] 448+2
     */
    public static double[] featureExtract(StateObservation obs){
        
        double[] feature = new double[452];  // 448 +4+1
        //Avatar position
        int Avatar_x= (int)obs.getAvatarPosition().x;
        int Avatar_y= (int)obs.getAvatarPosition().y;
        int min_MD=-1; //比较曼哈顿距离
        double x_y=-1.0;

        // 448 locations
        int[][] map = new int[32][14];
        // Extract features
        LinkedList<Observation> allobj = new LinkedList<>();
        if( obs.getImmovablePositions()!=null )
            for(ArrayList<Observation> l : obs.getImmovablePositions()) allobj.addAll(l);
        if( obs.getMovablePositions()!=null )
            for(ArrayList<Observation> l : obs.getMovablePositions()) allobj.addAll(l);
        if( obs.getNPCPositions()!=null ) {
            for (ArrayList<Observation> l : obs.getNPCPositions())
            {
                allobj.addAll(l);
                for(Observation o:l){
                    double xD=Math.abs(Avatar_x-o.position.x);
                    double yD=Math.abs(Avatar_y-o.position.y);
                    int MD=(int)((xD+yD)/25);//计算曼哈顿距离
                    if(min_MD<0||MD<min_MD)   //找最近的NPC
                    {
                        min_MD=MD;
                        x_y=xD/yD;
                    }
                }
            }
        }
        
        for(Observation o : allobj){
            Vector2d p = o.position;
            int x = (int)(p.x/25);
            int y= (int)(p.y/25);
            map[x][y] = o.itype;
        }
        for(int y=0; y<14; y++)
            for(int x=0; x<32; x++)
                feature[y*32+x] = map[x][y];

        //feature[448] = obs.getGameTick();
        feature[448] =(int)(Avatar_x/25);
        feature[449] = x_y; //距离Avatar最近的怪兽横纵比
        feature[450]= min_MD;
        return feature;
    }

    /**
     * 准备数据集的格式信息（需与提取的特征保持一致）---若特征提取函数修改需一并修改
     * - information of every position on the image: itype of object on every position(32*14)
     * - 4 states feature:GameTick,AvatarSpeed,AvatarHealthPoints,AvatarType
     * @return feature list[453] 448+4+1(class)
     */
    public static Instances datasetHeader(){
        FastVector attInfo = new FastVector();
        // 448 locations
        for(int y=0; y<14; y++){
            for(int x=0; x<32; x++){
                Attribute att = new Attribute("object_at_position_x=" + x + "_y=" + y);
                attInfo.addElement(att);
            }
        }
        //  information
        //Attribute att = new Attribute("GameTick" ); attInfo.addElement(att);
//        att = new Attribute("MinNPC_AvatarDis" ); attInfo.addElement(att);
        Attribute att = new Attribute("Avatar_x" ); attInfo.addElement(att);
        att = new Attribute("NPC_AvatarK" ); attInfo.addElement(att);
        att = new Attribute("MinNPC_AvatarDis" ); attInfo.addElement(att);

        //class
        FastVector classes = new FastVector();
        classes.addElement("0");
        classes.addElement("1");
        classes.addElement("2");
        classes.addElement("3");
        att = new Attribute("class", classes);        
        attInfo.addElement(att);
        
        Instances instances = new Instances("AliensData", attInfo, 0);
        instances.setClassIndex( instances.numAttributes() - 1);
        
        return instances;
    }
    
    // Record each move as the ARFF instance
    public void invoke(StateObservation obs, Types.ACTIONS action) {
        double[]  feature = featureExtract(obs);
        
        try{
            for(int i=0; i<feature.length-1; i++)
                filewriter.write(feature[i] + ",");
            // Recorde the move type as ARFF classes
            int action_num = 0;
            if( Types.ACTIONS.ACTION_NIL == action) action_num = 0;
            if( Types.ACTIONS.ACTION_USE == action) action_num = 1;
            if( Types.ACTIONS.ACTION_LEFT == action) action_num = 2;
            if( Types.ACTIONS.ACTION_RIGHT == action) action_num = 3;
            filewriter.write(action_num + "\n");
            filewriter.flush();
        }catch(Exception exc){
            exc.printStackTrace();
        }
    }
    
    public void close(){
        try{
            filewriter.close();
        }catch(Exception exc){
            exc.printStackTrace();
        }
    }
}
