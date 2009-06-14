/**
 * Copyright 2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nutch.admin.management;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.Path;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.nutch.admin.TaskThread;
import org.apache.nutch.crawl.Generator;


public class GenerateThread extends TaskThread {

  private static final Log LOG = 
    LogFactory.getLog(GenerateThread.class);

  private Generator fGenerator;

  private Path fCrawldb;

  private Path fSegmentFolder;

  private long fTopN;

  private long fDays;

  private int fFetcher;

  private boolean fError;

  public GenerateThread(Configuration configuration, Path crawldb,
      Path segmentFolder, String topN, String fetcher, String days) {
    
    super(configuration);
    
    this.fGenerator = new Generator(configuration);
    this.fCrawldb = crawldb;
    this.fSegmentFolder = segmentFolder;
    try {
      this.fTopN = Long.parseLong(topN);
      this.fDays = Long.parseLong(days);
      this.fFetcher = Integer.parseInt(fetcher);
    } catch (NumberFormatException e) {
      this.fMessage = "number.invalid";
      this.fError = true;
    }
  }

  public void run() {
    if (!this.fError) {
      FileSystem fileSystem = null;
      Path runningSegment = null;
      Path runningDB = null;
      try {
        this.fMessage = "generate.running";
        fileSystem = FileSystem.get(this.fConfiguration);
        runningSegment = new Path(this.fSegmentFolder, "generate.running");
        runningDB = new Path(this.fCrawldb, "generate.running");
        fileSystem.createNewFile(runningSegment);
        fileSystem.createNewFile(runningDB);
        long curTime = 
          System.currentTimeMillis() 
          + this.fDays * 1000L * 60 * 60 * 24;
        
        this.fGenerator.generate(
            this.fCrawldb,
            this.fSegmentFolder,
            this.fFetcher, 
            this.fTopN, 
            curTime,
            true,
            true
            );
        
      } catch (IOException e) {
        LOG.warn(e.toString());
        this.fMessage = e.toString();
      } finally {
        try {
          if (fileSystem != null) {
            fileSystem.delete(runningSegment);
            fileSystem.delete(runningDB);
          }
        } catch (IOException e) {
          LOG.warn(e.toString());
        }
      }
    }
  }
}
