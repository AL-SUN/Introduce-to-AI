/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.learningmodel;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.Vector2d;
import weka.core.*;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 * @author yuy
 */
public class RLDataExtractor {
    public FileWriter filewriter;
    public static Instances s_datasetHeader = datasetHeader();
    
    public RLDataExtractor(String filename) throws Exception{
        
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
    
    public static Instance makeInstance(double[] features, int action, double reward){
        features[features.length-2] = action;
        features[features.length-1] = reward;
        Instance ins = new Instance(1, features);
        ins.setDataset(s_datasetHeader);
        return ins;
    }
    public static double ManDis(Vector2d p1,Vector2d p2){
        return (Math.abs(p1.x-p2.x)+Math.abs(p1.y-p2.y))/28;
    }
    public static double[] featureExtract(StateObservation obs){
        
        double[] feature = new double[877];  // 868 + 4 + 3 + 1(action) + 1(Q)
        
        // 448 locations
        int[][] map = new int[28][31];
        // Extract features
        LinkedList<Observation> allobj = new LinkedList<>();
        if( obs.getImmovablePositions()!=null ) {
            for (ArrayList<Observation> l : obs.getImmovablePositions())
                allobj.addAll(l);
        }
        LinkedList<Observation> moveobj = new LinkedList<>();
        if( obs.getMovablePositions()!=null ){
            for(ArrayList<Observation> l : obs.getMovablePositions()) {
                allobj.addAll(l);
                moveobj.addAll(l);
            }
        }

        for(Observation o : allobj){
            Vector2d p = o.position;
            int x = (int)(p.x/28); //squre size is 20 for pacman
            int y= (int)(p.y/28);  //size is 28 for FreeWay
            map[x][y] = o.itype;
        }
        for(int y=0; y<31; y++)
            for(int x=0; x<28; x++)
                feature[y*28+x] = map[x][y];
        // 4 states
        feature[868] = obs.getGameTick();
        feature[869] = obs.getAvatarSpeed();
        feature[870] = obs.getAvatarHealthPoints();
        feature[871] = obs.getAvatarType();

        //3 new features
        double goalDis;
        double minTrackDis=100; //initial
        int trackType=0;

        if(obs.isGameOver()&&obs.getGameWinner()== Types.WINNER.PLAYER_WINS)
            goalDis=0;
        else {
            Vector2d Avatar_p = obs.getAvatarPosition();
            Vector2d Portal_p = new Vector2d(15 * 28.0, 28.0);    //目标位置，若目标不存在，假设在第一行中间
            if (obs.getPortalsPositions() != null) {
                for (ArrayList<Observation> l : obs.getPortalsPositions()) {
                    if (l.size() > 0) {
                        Portal_p = l.get(0).position;
                    } else
                        Portal_p = Avatar_p;
                }
            }
            goalDis=ManDis(Portal_p,Avatar_p);  //distance between Avatar and portal

            for(Observation o:moveobj){
                boolean direct=true;
                if(o.position.x>Avatar_p.x){  //right-->
                    if(o.itype==7||o.itype==8) //fastRtruck-7,slowRtruck8
                        direct=false;
                }
                else if(o.position.x<Avatar_p.x) {   //<--left
                    if (o.itype == 10 || o.itype == 11) //fastLtruck-10,lowLtruck-11
                        direct = false;
                }
                if(direct){
                    double MD=ManDis(o.position,Avatar_p);//计算曼哈顿距离
                    if(MD<minTrackDis) {  //找最近的Track
                        minTrackDis = MD;
                        trackType= o.itype;
                    }
                }
            }
        }

        feature[872] = goalDis;
        feature[873] = minTrackDis;
        feature[874] = trackType;

        return feature;
    }
    
    public static Instances datasetHeader(){
        
        if (s_datasetHeader!=null)
            return s_datasetHeader;
        
        FastVector attInfo = new FastVector();
        // 448 locations
        for(int y=0; y<28; y++){
            for(int x=0; x<31; x++){
                Attribute att = new Attribute("object_at_position_x=" + x + "_y=" + y);
                attInfo.addElement(att);
            }
        }
        Attribute att = new Attribute("GameTick" ); attInfo.addElement(att);
        att = new Attribute("AvatarSpeed" ); attInfo.addElement(att);
        att = new Attribute("AvatarHealthPoints" ); attInfo.addElement(att);
        att = new Attribute("AvatarType" ); attInfo.addElement(att);
        att = new Attribute("AvatarPortalDistance" ); attInfo.addElement(att);
        att = new Attribute("NearestTrackDistance" ); attInfo.addElement(att);
        att = new Attribute("NearestTrackType" ); attInfo.addElement(att);
        //action
        FastVector actions = new FastVector();
        actions.addElement("0");
        actions.addElement("1");
        actions.addElement("2");
        actions.addElement("3");
        att = new Attribute("actions", actions);        
        attInfo.addElement(att);
        // Q value
        att = new Attribute("Qvalue");
        attInfo.addElement(att);
        
        Instances instances = new Instances("PacmanQdata", attInfo, 0);
        instances.setClassIndex( instances.numAttributes() - 1);
        
        return instances;
    }
    
}
