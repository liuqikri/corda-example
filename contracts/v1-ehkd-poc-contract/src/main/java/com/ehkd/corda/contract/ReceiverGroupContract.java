package com.ehkd.corda.contract;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import static net.corda.core.contracts.ContractsDSL.requireThat;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
public class ReceiverGroupContract implements Contract  {
    private final static String ID = "com.ehkd.corda.contract.ReceiverGroupContract";

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
       final CommandData commandData = tx.getCommands().get(0).getValue();

        if(commandData instanceof ReceiverGroupContract.Commands.Create) {
            requireThat(require -> {
                require.using("This transaction should only have one state as output", tx.getOutputs().size() == 1);
                return null;
            });
        } else if(commandData instanceof ReceiverGroupContract.Commands.Update) {
            requireThat(require -> {
                require.using("This transaction should only have one state as output", tx.getOutputs().size() == 1);
                require.using("This transaction should only have one state as input", tx.getInputs().size() == 1);
                return null;
            });
        } else {
            //Unrecognized Command type
            throw new IllegalArgumentException("Incorrect type of AppleStamp Commands");
        }
    }

    public interface Commands extends CommandData {
        class Create implements ReceiverGroupContract.Commands {}
        class Update implements ReceiverGroupContract.Commands {}
    }
}
