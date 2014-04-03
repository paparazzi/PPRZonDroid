package com.PPRZonDroid;

/**
 * Created by savas on 2/1/14.
 */
public class BlockModel {


  private String BlockName;


  public BlockModel(String BlName) {
    super();
    this.BlockName = BlName;
  }

  public String getTitle() {
    return BlockName;
  }

  public void setTitle(String BlockName) {
    this.BlockName = BlockName;
  }


}