package rs.expand.pixelupgrade.commands;

import java.util.Arrays;
import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.configs.ForceStatsConfig;

public class ForceStats implements CommandExecutor
{
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
    {
        if (src instanceof Player)
        {
            Integer debugVerbosityMode;

            debugVerbosityMode = checkConfigInt(false);

            if (debugVerbosityMode == null)
            {
                printToLog(0, "Error parsing config! Make sure everything is valid, or regenerate it.");
                src.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid config for command! Please report this to staff."));
            }
            else
            {
                printToLog(2, "Called by player \u00A73" + src.getName() + "\u00A7b. Starting!");

                Boolean canContinue = true, statWasFixed = true, forceValue = false, shinyFix = false, valueIsInt = false;
                Integer slot = 0, intValue = null;
                String stat = null, fixedStat = null, value = null;

                if (!args.<String>getOne("slot").isPresent())
                {
                    printToLog(2, "No arguments provided, aborting.");

                    src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                    src.sendMessage(Text.of("\u00A74Error: \u00A7cNo parameters found. See below."));
                    src.sendMessage(Text.of("\u00A74Usage: \u00A7c/forcestats <slot> <stat> <value> (-f to force)"));
                    src.sendMessage(Text.of(""));
                    src.sendMessage(Text.of("\u00A75Please note: \u00A7dPassing the -f flag will disable safety checks."));
                    src.sendMessage(Text.of("\u00A7dThis may lead to crashes or even corruption. Handle with care!"));
                    src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

                    canContinue = false;
                }
                else
                {
                    String slotString = args.<String>getOne("slot").get();

                    if (slotString.matches("^[1-6]"))
                    {
                        printToLog(3, "Slot was a valid slot number. Let's move on!");
                        slot = Integer.parseInt(args.<String>getOne("slot").get());
                    }
                    else
                    {
                        printToLog(2, "Invalid slot provided. Aborting.");

                        src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                        src.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid slot value. Valid values are 1-6."));
                        src.sendMessage(Text.of("\u00A74Usage: \u00A7c/forcestats <slot> <stat> <value> (-f to force)"));
                        src.sendMessage(Text.of(""));
                        src.sendMessage(Text.of("\u00A75Please note: \u00A7dPassing the -f flag will disable safety checks."));
                        src.sendMessage(Text.of("\u00A7dThis may lead to crashes or even corruption. Handle with care!"));
                        src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

                        canContinue = false;
                    }
                }

                if (args.hasAny("f"))
                    forceValue = true;

                if (args.<String>getOne("stat").isPresent() && canContinue)
                {
                    stat = args.<String>getOne("stat").get();

                    switch (stat.toUpperCase())
                    {
                        case "IVHP":
                            fixedStat = "IVHP";
                            break;
                        case "IVATTACK":
                            fixedStat = "IVAttack";
                            break;
                        case "IVDEFENCE": case "IVDEFENSE":
                            fixedStat = "IVDefence";
                            break;
                        case "IVSPATT": case "IVSPATK":
                            fixedStat = "IVSpAtt";
                            break;
                        case "IVSPDEF":
                            fixedStat = "IVSpDef";
                            break;
                        case "IVSPEED":
                            fixedStat = "IVSpeed";
                            break;
                        case "EVHP":
                            fixedStat = "EVHP";
                            break;
                        case "EVATTACK":
                            fixedStat = "EVAttack";
                            break;
                        case "EVDEFENCE": case "EVDEFENSE":
                            fixedStat = "EVDefence";
                            break;
                        case "EVSPECIALATTACK": case "EVSPATT": case "EVSPATK":
                            fixedStat = "EVSpecialAttack";
                            break;
                        case "EVSPECIALDEFENCE": case "EVSPDEF":
                            fixedStat = "EVSpecialDefence";
                            break;
                        case "EVSPEED":
                            fixedStat = "EVSpeed";
                            break;
                        case "GROWTH": case "SIZE":
                            fixedStat = "Growth";
                            break;
                        case "NATURE":
                            fixedStat = "Nature";
                            break;
                        case "ISSHINY": case "IS_SHINY": case "SHINY":
                            fixedStat = "IsShiny";
                            shinyFix = true;
                            break;
                        default:
                            statWasFixed = false;
                    }

                    if (!statWasFixed && !forceValue)
                    {
                        printToLog(2, "Invalid stat provided, and force flag not passed. Abort.");

                        src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                        src.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid stat provided. See below for valid stats."));
                        src.sendMessage(Text.of("\u00A74Usage: \u00A7c/forcestats <slot> <stat> <value> (-f to force)"));
                        src.sendMessage(Text.of(""));
                        src.sendMessage(Text.of("\u00A76IVs: \u00A7eIVHP, IVAttack, IVDefence, IVSpAtt, IVSpDef, IVSpeed"));
                        src.sendMessage(Text.of("\u00A76EVs: \u00A7eEVHP, EVAttack, EVDefence, EVSpAtt, EVSpDef, EVSpeed"));
                        src.sendMessage(Text.of("\u00A76Others: \u00A7eGrowth, Nature, Shiny"));
                        src.sendMessage(Text.of(""));
                        src.sendMessage(Text.of("\u00A75Please note: \u00A7dPassing the -f flag will disable safety checks."));
                        src.sendMessage(Text.of("\u00A7dThis may lead to crashes or even corruption. Handle with care!"));
                        src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

                        canContinue = false;
                    }
                }
                else if (canContinue)
                {
                    printToLog(2, "No stat was provided. Let's abort.");

                    src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                    src.sendMessage(Text.of("\u00A74Error: \u00A7cNo stat provided. See below for valid stats."));
                    src.sendMessage(Text.of("\u00A74Usage: \u00A7c/forcestats <slot> <stat> <value> (-f to force)"));
                    src.sendMessage(Text.of(""));
                    src.sendMessage(Text.of("\u00A76IVs: \u00A7eIVHP, IVAttack, IVDefence, IVSpAtt, IVSpDef, IVSpeed"));
                    src.sendMessage(Text.of("\u00A76EVs: \u00A7eEVHP, EVAttack, EVDefence, EVSpAtt, EVSpDef, EVSpeed"));
                    src.sendMessage(Text.of("\u00A76Others: \u00A7eGrowth, Nature, Shiny"));
                    src.sendMessage(Text.of(""));
                    src.sendMessage(Text.of("\u00A75Please note: \u00A7dPassing the -f flag will disable safety checks."));
                    src.sendMessage(Text.of("\u00A7dThis may lead to crashes or even corruption. Handle with care!"));
                    src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

                    canContinue = false;
                }

                if (!args.<String>getOne("value").isPresent() && canContinue)
                {
                    printToLog(2, "No value was provided. Let's abort.");

                    src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                    src.sendMessage(Text.of("\u00A74Error: \u00A7cNo value or amount was provided."));
                    src.sendMessage(Text.of("\u00A74Usage: \u00A7c/forcestats <slot> <stat> <value> (-f to force)"));
                    src.sendMessage(Text.of(""));
                    src.sendMessage(Text.of("\u00A75Please note: \u00A7dPassing the -f flag will disable safety checks."));
                    src.sendMessage(Text.of("\u00A7dThis may lead to crashes or even corruption. Handle with care!"));
                    src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));

                    canContinue = false;
                }
                else if (canContinue)
                {
                    String valueString = args.<String>getOne("value").get();

                    if (valueString.matches("^-?[0-9].*"))
                    {
                        printToLog(3, "Checked value, and found out it's an integer. Setting flag.");
                        intValue = Integer.parseInt(args.<String>getOne("value").get());
                        valueIsInt = true;
                    }
                    else
                    {
                        printToLog(3, "Value is not an integer, so treating it as a string.");
                        value = args.<String>getOne("value").get();
                    }
                }

                if (canContinue)
                {
                    printToLog(3, "No error encountered, input should be valid. Continuing!");
                    Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorage(((EntityPlayerMP) src));

                    if (!storage.isPresent())
                    {
                        printToLog(0, "\u00A74" + src.getName() + "\u00A7c does not have a Pixelmon storage, aborting. May be a bug?");
                        src.sendMessage(Text.of("\u00A74Error: \u00A7cNo Pixelmon storage found. Please contact staff!"));
                    }
                    else
                    {
                        PlayerStorage storageCompleted = storage.get();
                        NBTTagCompound nbt = storageCompleted.partyPokemon[slot - 1];

                        if (nbt == null)
                        {
                            printToLog(2, "No NBT found in slot, probably empty. Aborting...");
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cYou don't have anything in that slot!"));
                        }
                        else if (!forceValue && valueIsInt)
                        {
                            String[] validIVEV = new String[]{"IVHP", "IVAttack", "IVDefence", "IVSpAtt", "IVSpDef", "IVSpeed", "EVHP", "EVAttack", "EVDefence", "EVSpecialAttack", "EVSpecialDefence", "EVSpeed"};

                            printToLog(3, "Value is not forced, but is valid. Let's patch up the player's input.");
                            printToLog(3, "Found stat \u00A72" + stat + "\u00A7a, trying adjustment... It is now \u00A72" + fixedStat + "\u00A7a.");

                            if (Arrays.asList(validIVEV).contains(fixedStat) && intValue > 32767 || Arrays.asList(validIVEV).contains(fixedStat) && intValue < -32768)
                            {
                                printToLog(2, "Found an IV or EV so high that it'd roll over. Aborting.");
                                src.sendMessage(Text.of("\u00A74Error: \u00A7cIV/EV value out of bounds. Valid range: -32768 ~ 32767"));
                            }
                            else if (fixedStat.equals("Growth") && intValue > 8 || fixedStat.equals("Growth") && intValue < 0)
                            {
                                printToLog(2, "Found a Growth value above 8 or below 0; out of bounds. Aborting.");
                                src.sendMessage(Text.of("\u00A74Error: \u00A7cSize value out of bounds. Valid range: 0 ~ 8"));
                            }
                            else if (fixedStat.equals("Nature") && intValue > 24 || fixedStat.equals("Nature") && intValue < 0)
                            {
                                printToLog(2, "Found a Nature value above 24 or below 0; out of bounds. Aborting.");
                                src.sendMessage(Text.of("\u00A74Error: \u00A7cNature value out of bounds. Valid range: 0 ~ 24"));
                            }
                            else if (fixedStat.equals("IsShiny") && intValue != 0 && intValue != 1)
                            {
                                printToLog(2, "Invalid shiny status value detected. Aborting.");
                                src.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid boolean value. Valid values: 0 (=false) or 1 (=true)"));
                            }
                            else
                            {
                                printToLog(1, "Changing value. Stat: \u00A76" + fixedStat + "\u00A7e. Old: \u00A76" + nbt.getInteger(fixedStat) + "\u00A7e. New: \u00A76" + intValue + "\u00A7e.");

                                nbt.setInteger(fixedStat, intValue);
                                if (shinyFix)
                                    nbt.setInteger("Shiny", intValue);

                                src.sendMessage(Text.of("\u00A7aValue changed! Not showing? Reconnect to update your client."));
                            }
                        }
                        else if (!forceValue)
                        {
                            printToLog(2, "Provided value was a string, but they're only supported in force mode. Abort.");

                            src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cGot a non-integer value, but no flag. Try a number."));
                            src.sendMessage(Text.of("\u00A74Usage: \u00A7c/forcestats <slot> <stat> <value> (-f to force)"));
                            src.sendMessage(Text.of(""));
                            src.sendMessage(Text.of("\u00A75Please note: \u00A7dPassing the -f flag will disable safety checks."));
                            src.sendMessage(Text.of("\u00A7dThis may lead to crashes or even corruption. Handle with care!"));
                            src.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
                        }
                        else
                        {
                            try
                            {
                                printToLog(1, "Value is being forced! Old value: \u00A76" + nbt.getInteger(stat) + "\u00A7e.");
                            }
                            catch (Exception F)
                            {
                                printToLog(1, "Value is being forced! Tried to grab old value, but couldn't read it...");
                            }

                            src.sendMessage(Text.of("\u00A7eForcing value..."));

                            if (statWasFixed)
                            {
                                printToLog(3, "Found a known stat in force mode. Checking and fixing, just in case...");

                                src.sendMessage(Text.of("\u00A75Note: \u00AddA known stat was found, and was checked and possibly corrected."));
                                src.sendMessage(Text.of("\u00A75Provided stat: \u00Add" + stat + "\u00A75, changed to: \u00A7d" + fixedStat));

                                stat = fixedStat;
                            }

                            if (valueIsInt)
                            {
                                printToLog(1, "Integer value written. Glad to be of service.");

                                nbt.setInteger(stat, intValue);
                                if (shinyFix)
                                    nbt.setInteger("Shiny", intValue);
                            }
                            else
                            {
                                printToLog(1, "String value written. Glad to be of service.");

                                nbt.setString(stat, value);
                                if (shinyFix)
                                    nbt.setString("Shiny", value);
                            }

                            src.sendMessage(Text.of("\u00A7aThe new value was written, a reconnect may be necessary."));
                        }
                    }
                }
            }
        }
        else
            printToLog(0, "This command cannot run from the console or command blocks.");

        return CommandResult.success();
	}

    private void printToLog(Integer debugNum, String inputString)
    {
        Integer debugVerbosityMode = checkConfigInt(true);

        if (debugVerbosityMode == null)
            debugVerbosityMode = 4;

        if (debugNum <= debugVerbosityMode)
        {
            if (debugNum == 0)
                PixelUpgrade.log.info("\u00A74ForceStats // critical: \u00A7c" + inputString);
            else if (debugNum == 1)
                PixelUpgrade.log.info("\u00A76ForceStats // important: \u00A7e" + inputString);
            else if (debugNum == 2)
                PixelUpgrade.log.info("\u00A73ForceStats // start/end: \u00A7b" + inputString);
            else
                PixelUpgrade.log.info("\u00A72ForceStats // debug: \u00A7a" + inputString);
        }
    }

    private Integer checkConfigInt(Boolean noMessageMode)
    {
        if (!ForceStatsConfig.getInstance().getConfig().getNode("debugVerbosityMode").isVirtual())
            return ForceStatsConfig.getInstance().getConfig().getNode("debugVerbosityMode").getInt();
        else if (noMessageMode)
            return null;
        else
        {
            PixelUpgrade.log.info("\u00A74ForceStats // critical: \u00A7cCould not parse config variable \"debugVerbosityMode\"!");
            return null;
        }
    }
}