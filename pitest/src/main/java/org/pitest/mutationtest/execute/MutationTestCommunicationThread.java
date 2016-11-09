/*
 * Copyright 2011 Henry Coles
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

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.pitest.functional.SideEffect1;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.util.CommunicationThread;
import org.pitest.util.Id;
import org.pitest.util.Log;
import org.pitest.util.ReceiveStrategy;
import org.pitest.util.SafeDataInputStream;
import org.pitest.util.SafeDataOutputStream;

public class MutationTestCommunicationThread extends CommunicationThread {

  private static final Logger LOG = Log.getLogger();

  private static class SendData implements SideEffect1<SafeDataOutputStream> {
    private final MinionArguments arguments;

    SendData(final MinionArguments arguments) {
      this.arguments = arguments;
    }

    @Override
    public void apply(final SafeDataOutputStream dos) {
      dos.write(this.arguments);
      dos.flush();
    }
  }

  private static class Receive implements ReceiveStrategy {

    private final Map<MutationIdentifier, MutationStatusTestPair> idMap;
    private final Map<MutationIdentifier, List<MutationStatusTestPair>> allPairsMap;

    Receive(final Map<MutationIdentifier, MutationStatusTestPair> idMap,
            final Map<MutationIdentifier, List<MutationStatusTestPair>> allPairsMap) {
      this.idMap = idMap;
      this.allPairsMap = allPairsMap;
    }

    @Override
    public void apply(final byte control, final SafeDataInputStream is) {
      switch (control) {
      case Id.DESCRIBE:
        handleDescribe(is);
        break;
      case Id.REPORT:
        handleReport(is);
        break;
      }
    }

    private void handleReport(final SafeDataInputStream is) {
      final MutationIdentifier mutation = is.read(MutationIdentifier.class);
      final MutationStatusTestPair value = is
          .read(MutationStatusTestPair.class);
      if (value.getKillingTest().hasSome()) {
        pairsOfMutationId(mutation).add(value);
      }
      this.idMap.put(mutation, value);
      LOG.fine(mutation + " " + value);
    }

    private void handleDescribe(final SafeDataInputStream is) {
      final MutationIdentifier mutation = is.read(MutationIdentifier.class);
      this.idMap.put(mutation, new MutationStatusTestPair(1,
          DetectionStatus.STARTED));
    }

    private List<MutationStatusTestPair> pairsOfMutationId(MutationIdentifier mid)
    {
      if (allPairsMap.containsKey(mid)) {
        return allPairsMap.get(mid);
      }
      List<MutationStatusTestPair> entry = new ArrayList<MutationStatusTestPair>();
      allPairsMap.put(mid, entry);
      return entry;
    }
  }

  private final Map<MutationIdentifier, MutationStatusTestPair> idMap;
  private final Map<MutationIdentifier, List<MutationStatusTestPair>> allPairsMap;


  public MutationTestCommunicationThread(final ServerSocket socket,
      final MinionArguments arguments,
      final Map<MutationIdentifier, MutationStatusTestPair> idMap,
      final Map<MutationIdentifier, List<MutationStatusTestPair> > allPairsMap) {
    super(socket, new SendData(arguments), new Receive(idMap, allPairsMap));
    this.idMap = idMap;
    this.allPairsMap = allPairsMap;
  }

  public List<MutationStatusTestPair> getStatus(final MutationIdentifier id) {
    if (! this.idMap.containsKey(id)) {
      return null;
    }
    List<MutationStatusTestPair> returnValue = new ArrayList<MutationStatusTestPair>();
    if (this.idMap.get(id).getKillingTest().hasNone()) {
      returnValue.add(this.idMap.get(id));
    }
    if (this.allPairsMap.containsKey(id)) {
      returnValue.addAll(allPairsMap.get(id));
    }
    return returnValue;
  }

}
