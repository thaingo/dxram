package de.hhu.bsinfo.dxram.job;

import java.util.List;

import org.junit.Assert;
import org.junit.runner.RunWith;

import de.hhu.bsinfo.dxram.BeforeTestInstance;
import de.hhu.bsinfo.dxram.DXRAM;
import de.hhu.bsinfo.dxram.DXRAMJunitRunner;
import de.hhu.bsinfo.dxram.DXRAMTestConfiguration;
import de.hhu.bsinfo.dxram.TestInstance;
import de.hhu.bsinfo.dxram.boot.BootService;
import de.hhu.bsinfo.dxram.util.NodeRole;
import de.hhu.bsinfo.dxutils.NodeID;

@RunWith(DXRAMJunitRunner.class)
@DXRAMTestConfiguration(
        nodes = {
                @DXRAMTestConfiguration.Node(nodeRole = NodeRole.SUPERPEER),
                @DXRAMTestConfiguration.Node(nodeRole = NodeRole.PEER, enableJobService = true),
                @DXRAMTestConfiguration.Node(nodeRole = NodeRole.PEER, enableJobService = true)
        })
public class JobServiceRemote2Test {

    @BeforeTestInstance(runOnNodeIdx = {1, 2})
    public void registerJobType(final DXRAM p_instance) {
        p_instance.getService(JobService.class).registerJobType(JobRemoteSerializeTest.MS_TYPE_ID,
                JobRemoteSerializeTest.class);
    }

    @TestInstance(runOnNodeIdx = 2)
    public void remoteTest(final DXRAM p_instance) {
        JobService jobService = p_instance.getService(JobService.class);
        BootService bootService = p_instance.getService(BootService.class);

        List<Short> peers = bootService.getOnlinePeerNodeIDs();
        short otherPeer = NodeID.INVALID_ID;

        for (short peer : peers) {
            if (peer != bootService.getNodeID()) {
                otherPeer = peer;
                break;
            }
        }

        Assert.assertNotEquals(NodeID.INVALID_ID, otherPeer);

        JobRemoteSerializeTest job = new JobRemoteSerializeTest();

        long jobId = jobService.pushJobRemote(job, otherPeer);
        Assert.assertNotEquals(JobID.INVALID_ID, jobId);

        Assert.assertTrue(jobService.waitForRemoteJobsToFinish());
    }
}