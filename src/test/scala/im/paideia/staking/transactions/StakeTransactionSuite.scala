package im.paideia.staking.transactions

import org.scalatest.funsuite.AnyFunSuite
import im.paideia.staking.StakingConfig
import im.paideia.staking.TotalStakingState
import org.ergoplatform.appkit.Address
import org.ergoplatform.appkit.RestApiErgoClient
import org.ergoplatform.appkit.NetworkType
import org.ergoplatform.appkit.BlockchainContext
import org.ergoplatform.appkit.impl.BlockchainContextImpl
import im.paideia.staking.StakeStateBox
import org.ergoplatform.appkit.ErgoToken
import im.paideia.staking.StakeTransaction
import scala.collection.JavaConverters._
import org.ergoplatform.appkit.InputBox

class StakeTransactionSuite extends AnyFunSuite {
    test("Sign stake tx on empty state") {
        val config = StakingConfig.test
        val state = TotalStakingState(config, 0L)
        val dummyAddress = Address.create("4MQyML64GnzMxZgm")
        val ergoClient = RestApiErgoClient.create("http://ergolui.com:9053",NetworkType.MAINNET,"","https://api.ergoplatform.com")
        ergoClient.execute(new java.util.function.Function[BlockchainContext,Unit] {
            override def apply(_ctx: BlockchainContext): Unit = {
                val ctx = _ctx.asInstanceOf[BlockchainContextImpl]
                val stakeStateInput = StakeStateBox(ctx,state,100000000L).inputBox(ctx)
                val userInput = ctx.newTxBuilder().outBoxBuilder()
                    .contract(dummyAddress.toErgoContract())
                    .value(10000000000L)
                    .tokens(new ErgoToken(state.stakingConfig.stakedTokenId,10000000L))
                    .build().convertToInputWith("ce552663312afc2379a91f803c93e2b10b424f176fbc930055c10def2fd88a5d",2)

                val stakeTransaction = StakeTransaction(ctx,stakeStateInput,userInput,1000L,state,dummyAddress.getErgoAddress())
                ctx.newProverBuilder().build().sign(stakeTransaction.unsigned())
            }
        })
    }
}