package im.paideia.staking.transactions

import org.scalatest.funsuite.AnyFunSuite
import im.paideia.staking.StakingConfig
import im.paideia.staking.TotalStakingState
import org.ergoplatform.appkit.Address
import org.ergoplatform.appkit.RestApiErgoClient
import org.ergoplatform.appkit.NetworkType
import org.ergoplatform.appkit.BlockchainContext
import org.ergoplatform.appkit.impl.BlockchainContextImpl
import im.paideia.staking.boxes._
import org.ergoplatform.appkit.ErgoToken
import scala.collection.JavaConverters._
import org.ergoplatform.appkit.InputBox
import im.paideia.staking.transactions._
import im.paideia.governance.DAOConfig
import im.paideia.util.Util
import im.paideia.common.PaideiaTestSuite

class AddStakeTransactionSuite extends PaideiaTestSuite {
    test("Sign add stake tx") {
        val stakingConfig = StakingConfig.test
        val daoConfig = DAOConfig.test
        val state = TotalStakingState(stakingConfig, 0L)
        val testKey = Util.randomKey
        state.stake(testKey,100L)
        val dummyAddress = Address.create("4MQyML64GnzMxZgm")
        val ergoClient = createMockedErgoClient(MockData(Nil,Nil))
        ergoClient.execute(new java.util.function.Function[BlockchainContext,Unit] {
            override def apply(_ctx: BlockchainContext): Unit = {
                val ctx = _ctx.asInstanceOf[BlockchainContextImpl]
                val stakeStateInput = StakeStateBox(ctx,state,100000000L,daoConfig).inputBox()
                val stakingConfigInput = StakingConfigBox(ctx,stakingConfig,daoConfig).inputBox()
                val userInput = ctx.newTxBuilder().outBoxBuilder()
                    .contract(dummyAddress.toErgoContract())
                    .value(10000000000L)
                    .tokens(new ErgoToken(state.stakingConfig.stakedTokenId,10000000L),new ErgoToken(testKey,1L))
                    .build().convertToInputWith("ce552663312afc2379a91f803c93e2b10b424f176fbc930055c10def2fd88a5d",2)

                val addStakeTransaction = AddStakeTransaction(ctx,stakeStateInput,stakingConfigInput,userInput,testKey,1000L,state,dummyAddress.getErgoAddress(),daoConfig)
                ctx.newProverBuilder().build().sign(addStakeTransaction.unsigned())
            }
        })
    }
}
