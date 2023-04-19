package com.ehkd.corda.contract;

import com.ehkd.corda.state.TokenState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import static net.corda.core.contracts.ContractsDSL.requireThat;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
public class TokenContract implements Contract  {
    public final static String ID = "com.ehkd.corda.contract.TokenContract" ;

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
       final CommandData commandData = tx.getCommands().get(0).getValue();

        if(commandData instanceof TokenContract.Commands.Create) {
            requireThat(require -> {
                require.using("This transaction should have no state as input", tx.getInputs().size() == 0);
                require.using("This transaction should only have one state as output", tx.getOutputs().size() == 1);
                return null;
            });
        } else if(commandData instanceof TokenContract.Commands.BatchCreate) {
            requireThat(require -> {
                require.using("This transaction should only have one state as input", tx.getInputs().size() == 0);
                require.using("This transaction should only have one state as output", tx.getOutputs().size() >= 1);
                return null;
            });
        } else if(commandData instanceof TokenContract.Commands.TokenPayment) {
            requireThat(require -> {
                require.using("This transaction should have more than one state as output", tx.getOutputs().size() >= 1);
                require.using("This transaction should have more than one state as input", tx.getInputs().size() >= 1);
                Long inputAmount = 0L;
                for(int i = 0;i < tx.getInputs().size();i++){
                    TokenState inputState = (TokenState)tx.getInputs().get(i).getState().getData();
                    inputAmount = inputAmount + inputState.getAmount();
                }
                Long outputAmount = 0L;
                for(int i = 0;i < tx.getInputs().size();i++){
                    TokenState outputState = (TokenState)tx.getInputs().get(i).getState().getData();
                    outputAmount = outputAmount + outputState.getAmount();
                }
                require.using("This balance in output is not correct", inputAmount.equals(outputAmount));
                return null;
            });
        }else if(commandData instanceof TokenContract.Commands.TokenSplitPayment) {
            requireThat(require -> {
                require.using("This transaction should have more than one state as output", tx.getOutputs().size() >= 1);
                require.using("This transaction should have more than one state as input", tx.getInputs().size() >= 1);
                Long inputAmount = 0L;
                for(int i = 0;i < tx.getInputs().size();i++){
                    TokenState inputState = (TokenState)tx.getInputs().get(i).getState().getData();
                    inputAmount = inputAmount + inputState.getAmount();
                }
                Long outputAmount = 0L;
                for(int i = 0;i < tx.getInputs().size();i++){
                    TokenState outputState = (TokenState)tx.getInputs().get(i).getState().getData();
                    outputAmount = outputAmount + outputState.getAmount();
                }
                require.using("This balance in output is not correct", inputAmount.equals(outputAmount));
                return null;
            });
        }else if(commandData instanceof TokenContract.Commands.TokenSplitPaymentFinal) {
            requireThat(require -> {
                require.using("This transaction should have more than one state as output", tx.getOutputs().size() >= 1);
                require.using("This transaction should have more than one state as input", tx.getInputs().size() >= 1);
                Long inputAmount = 0L;
                for(int i = 0;i < tx.getInputs().size();i++){
                    TokenState inputState = (TokenState)tx.getInputs().get(i).getState().getData();
                    inputAmount = inputAmount + inputState.getAmount();
                }
                Long outputAmount = 0L;
                for(int i = 0;i < tx.getInputs().size();i++){
                    TokenState outputState = (TokenState)tx.getInputs().get(i).getState().getData();
                    outputAmount = outputAmount + outputState.getAmount();
                }
                require.using("This balance in output is not correct", inputAmount.equals(outputAmount));
                return null;
            });
        } else if(commandData instanceof TokenContract.Commands.Beat) {
            requireThat(require -> {
                require.using("This transaction should have more than one state as input", tx.getInputs().size() >= 1);
                require.using("This transaction should have no state as output", tx.getOutputs().size() == 0);
                return null;
            });
        } else {
            //Unrecognized Command type
            throw new IllegalArgumentException("Incorrect type of AppleStamp Commands");
        }
    }

    public interface Commands extends CommandData {
        class Create implements TokenContract.Commands {}
        class BatchCreate implements TokenContract.Commands {}
        class TokenPayment implements TokenContract.Commands {}
        class TokenSplitPayment implements TokenContract.Commands {}
        class TokenSplitPaymentFinal implements TokenContract.Commands {}
        class Beat implements TokenContract.Commands {}
    }
}
