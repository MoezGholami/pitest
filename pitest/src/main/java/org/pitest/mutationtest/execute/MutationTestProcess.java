package org.pitest.mutationtest.execute;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.List;

import org.pitest.mutationtest.MutationStatusMap;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.process.ProcessArgs;
import org.pitest.process.WrappingProcess;
import org.pitest.util.ExitCode;

public class MutationTestProcess {

  private final WrappingProcess                 process;
  private final MutationTestCommunicationThread thread;

  public MutationTestProcess(final ServerSocket socket,
      final ProcessArgs processArgs, final MinionArguments arguments) {
    this.process = new WrappingProcess(socket.getLocalPort(), processArgs,
        MutationTestMinion.class);
    this.thread = new MutationTestCommunicationThread(socket, arguments,
        new HashMap<MutationIdentifier, MutationStatusTestPair>(),
        new HashMap<MutationIdentifier, List<MutationStatusTestPair>>());

  }

  public void start() throws IOException, InterruptedException {
    this.thread.start();
    this.process.start();
  }

  public void results(final MutationStatusMap allmutations) throws IOException {

    for (final MutationDetails each : allmutations.allMutations()) {
      final List<MutationStatusTestPair> allStatuses = this.thread.getStatus(each.getId());
      if (allStatuses != null) {
        for (MutationStatusTestPair status : allStatuses) {
          allmutations.setStatusForMutation(each, status);
        }
      }
    }

  }

  public ExitCode waitToDie() {
    try {
      return this.thread.waitToFinish();
    } finally {
      this.process.destroy();
    }

  }

}
