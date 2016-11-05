/*
 * Copyright 2010 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.mutationtest.execute;

import org.pitest.functional.Option;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.testapi.Description;
import org.pitest.testapi.TestListener;
import org.pitest.testapi.TestResult;

import java.util.ArrayList;
import java.util.List;

public class CheckTestHasFailedResultListener implements TestListener {

  private Option<Description> lastFailingTest = Option.none();
  private ArrayList<Option<Description>> allTestsRun = new ArrayList<Option<Description>>();
  private int                 testsRun        = 0;

  public List<MutationStatusTestPair> getAllTestResults() {
    List<MutationStatusTestPair> result = new ArrayList<MutationStatusTestPair>();
    for (Option<Description> d : allTestsRun) {
      result.add(new MutationStatusTestPair(testsRun,
              d.hasSome() ? DetectionStatus.KILLED : DetectionStatus.KILLED.SURVIVED,
              d.value().getQualifiedName()));
    }
    return result;
  }

  @Override
  public void onTestFailure(final TestResult tr) {
    this.lastFailingTest = Option.some(tr.getDescription());
    allTestsRun.add(Option.some(tr.getDescription()));
  }

  @Override
  public void onTestSkipped(final TestResult tr) {

  }

  @Override
  public void onTestStart(final Description d) {
    this.testsRun++;
  }

  @Override
  public void onTestSuccess(final TestResult tr) {
    allTestsRun.add(Option.some(tr.getDescription()));

  }

  @Override
  public void onRunEnd() {

  }

  @Override
  public void onRunStart() {

  }

}
