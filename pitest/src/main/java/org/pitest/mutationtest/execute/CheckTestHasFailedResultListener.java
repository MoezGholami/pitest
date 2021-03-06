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
  private ArrayList<TestResult> allTestResults = new ArrayList<TestResult>();
  private int                 testsRun        = 0;

  public List<MutationStatusTestPair> getAllTestResults() {
    List<MutationStatusTestPair> result = new ArrayList<MutationStatusTestPair>();
    Option<Description> d;
    DetectionStatus status;
    if (lastFailingTest.hasSome()) {
      for (TestResult tr : allTestResults) {
        if (tr.getThrowable().toString().contains("TimeoutException")
                || tr.getThrowable().toString().contains("test timed out")) {
          status = DetectionStatus.TIMED_OUT;
        } else {
          status = DetectionStatus.KILLED;
        }
        d = Option.some(tr.getDescription());
        result.add(new MutationStatusTestPair(testsRun, status,
                d.value().getQualifiedName()));
      }
    } else {
      result.add(new MutationStatusTestPair(testsRun, DetectionStatus.SURVIVED));
    }
    return result;
  }

  @Override
  public void onTestFailure(final TestResult tr) {
    this.lastFailingTest = Option.some(tr.getDescription());
    allTestResults.add(tr);
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

  }

  @Override
  public void onRunEnd() {

  }

  @Override
  public void onRunStart() {

  }
  public DetectionStatus status() {
    if (this.lastFailingTest.hasSome()) {
      return DetectionStatus.KILLED;
    } else {
    return DetectionStatus.SURVIVED;
    }
  }

  public Option<Description> lastFailingTest() {
    return this.lastFailingTest;
  }

  public int getNumberOfTestsRun() {
    return this.testsRun;
  }

}
