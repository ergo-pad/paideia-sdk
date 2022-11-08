package im.paideia.governance.transactions

import im.paideia.common.PaideiaTestSuite
import org.ergoplatform.appkit.BlockchainContext
import org.ergoplatform.appkit.impl.BlockchainContextImpl
import im.paideia.util.Util
import im.paideia.DAOConfig
import im.paideia.Paideia
import im.paideia.util.ConfKeys
import org.ergoplatform.appkit.ErgoId
import im.paideia.DAO
import im.paideia.governance.contracts.ProtoDAO
import im.paideia.common.contracts.PaideiaContractSignature
import org.ergoplatform.restapi.client.ErgoTransaction
import im.paideia.common.TransactionEvent
import im.paideia.governance.contracts.Mint
import im.paideia.util.Env

class CreateDAOTransactionSuite extends PaideiaTestSuite {
    test("Create DAO") {
        val ergoClient = createMockedErgoClient(MockData(Nil,Nil))
        ergoClient.execute(new java.util.function.Function[BlockchainContext,Unit] {
            override def apply(_ctx: BlockchainContext): Unit = {
                val ctx = _ctx.asInstanceOf[BlockchainContextImpl]
                PaideiaTestSuite.init(ctx)
                val daoKey = Util.randomKey
                val config = DAOConfig()
                val daoGovTokenId = Util.randomKey
                val proposalTokenId = Util.randomKey
                val actionTokenId = Util.randomKey
                val voteTokenId = Util.randomKey
                val stakeStateTokenId = Util.randomKey
                config.set(ConfKeys.im_paideia_dao_name,"Test DAO")
                config.set(ConfKeys.im_paideia_dao_tokenid,ErgoId.create(daoGovTokenId).getBytes())
                config.set(ConfKeys.im_paideia_dao_proposal_tokenid,ErgoId.create(proposalTokenId).getBytes())
                config.set(ConfKeys.im_paideia_dao_action_tokenid,ErgoId.create(actionTokenId).getBytes())
                config.set(ConfKeys.im_paideia_dao_vote_tokenid,ErgoId.create(voteTokenId).getBytes())
                config.set(ConfKeys.im_paideia_dao_key,ErgoId.create(daoKey).getBytes())
                config.set(ConfKeys.im_paideia_staking_state_tokenid,ErgoId.create(stakeStateTokenId).getBytes())
                config.set(ConfKeys.im_paideia_staking_profit_tokenids,Array[Array[Byte]]())
                config.set(ConfKeys.im_paideia_staking_emission_delay,4L)
                config.set(ConfKeys.im_paideia_staking_emission_amount,100000L)
                config.set(ConfKeys.im_paideia_staking_cyclelength,1000000L)
                config.set(ConfKeys.im_paideia_staking_profit_thresholds,Array(0L,0L))
                Paideia.addDAO(new DAO(daoKey,config))
                val mintContract = Mint(PaideiaContractSignature(daoKey=Env.paideiaDaoKey))
                mintContract.newBox(mintContract.box(ctx,proposalTokenId,Long.MaxValue,"Test DAO Proposal","Test DAO Proposal",0).inputBox(),false)
                mintContract.newBox(mintContract.box(ctx,actionTokenId,Long.MaxValue,"Test DAO Action","Test DAO Action",0).inputBox(),false)
                mintContract.newBox(mintContract.box(ctx,voteTokenId,Long.MaxValue,"Test DAO Vote","Test DAO Vote",0).inputBox(),false)
                mintContract.newBox(mintContract.box(ctx,daoKey,1L,"Test DAO Key","Test DAO Key",0).inputBox(),false)
                mintContract.newBox(mintContract.box(ctx,stakeStateTokenId,1L,"Test DAO Stake State","Test DAO Stake State",0).inputBox(),false)

                val protoDAOContract = ProtoDAO(PaideiaContractSignature(daoKey=Env.paideiaDaoKey))
                val protoDAOBox = protoDAOContract.box(ctx,Paideia.getDAO(daoKey),1L,5000000L).ergoTransactionOutput()
                val dummyTx = (new ErgoTransaction()).addOutputsItem(protoDAOBox)
                val eventResponse = Paideia.handleEvent(TransactionEvent(ctx,false,dummyTx))
                assert(eventResponse.unsignedTransactions.size===1)
                ctx.newProverBuilder().build().sign(eventResponse.unsignedTransactions(0))
            }
        })
    }
}
