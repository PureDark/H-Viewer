package com.sina.util.dnscache.score;

import com.sina.util.dnscache.model.DomainModel;
import com.sina.util.dnscache.model.IpModel;

import java.util.ArrayList;

/**
 * Created by fenglei on 15/4/21.
 */
public interface IScore {

    public String[] serverIpScore(DomainModel domainModel) ;
    
    public String[] ListToArr(ArrayList<IpModel> list) ;
}
