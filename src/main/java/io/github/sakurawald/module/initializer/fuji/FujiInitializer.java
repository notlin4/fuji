package io.github.sakurawald.module.initializer.fuji;

import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.Fuji;
import io.github.sakurawald.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.LocaleHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.processor.CommandAnnotationProcessor;
import io.github.sakurawald.core.command.structure.CommandDescriptor;
import io.github.sakurawald.core.config.Configs;
import io.github.sakurawald.core.job.abst.BaseJob;
import io.github.sakurawald.core.manager.Managers;
import io.github.sakurawald.core.structure.CommandNodeEntry;
import io.github.sakurawald.core.structure.Pair;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.fuji.gui.AboutGui;
import io.github.sakurawald.module.initializer.fuji.gui.CommandDescriptorGui;
import io.github.sakurawald.module.initializer.fuji.gui.ModulesGui;
import io.github.sakurawald.module.initializer.fuji.gui.ServerCommandsGui;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@CommandNode("fuji")
@CommandRequirement(level = 4)
public class FujiInitializer extends ModuleInitializer {

    @CommandNode("reload")
    private static int $reload(@CommandSource CommandContext<ServerCommandSource> ctx) {
        // reload main-control file
        Configs.configHandler.readStorage();

        // reload modules
        Managers.getModuleManager().reloadModuleInitializers();

        // reload jobs
        BaseJob.rescheduleAll();

        LocaleHelper.sendMessageByKey(ctx.getSource(), "reload");
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("about")
    private static int $about(@CommandSource ServerPlayerEntity player) {
        ModMetadata metadata = FabricLoader.getInstance().getModContainer(Fuji.MOD_ID)
            .orElseThrow(() -> new IllegalStateException("failed to get the metadata of this mod."))
            .getMetadata();

        List<Person> persons = new ArrayList<>();
        persons.addAll(metadata.getAuthors());
        persons.addAll(metadata.getContributors());

        new AboutGui(player, persons, 0).open();
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("inspect server-commands")
    private static int $listServerCommands(@CommandSource ServerPlayerEntity player) {
        List<CommandNodeEntry> entities = CommandHelper.getCommandNodes().stream()
            .map(CommandNodeEntry::new)
            .sorted(Comparator.comparing(CommandNodeEntry::getPath))
            .toList();
        new ServerCommandsGui(player, entities, 0).open();
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("inspect modules")
    private static int $listModules(@CommandSource ServerPlayerEntity player) {
        List<Pair<String, Boolean>> list = Managers.getModuleManager().getModule2enable()
            .entrySet()
            .stream()
            .map(it -> new Pair<>(ReflectionUtil.joinModulePath(it.getKey()), it.getValue()))
            .sorted(Comparator.comparing(Pair::getKey))
            .toList();

        new ModulesGui(player, list, 0).open();
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("inspect fuji-commands")
    private static int $listFujiCommands(@CommandSource ServerPlayerEntity player) {
        List<CommandDescriptor> descriptors = CommandAnnotationProcessor
            .descriptors
            .stream()
            .sorted(Comparator.comparing(CommandDescriptor::buildCommandNodePath))
            .toList();

        new CommandDescriptorGui(player, descriptors, 0).open();

        return CommandHelper.Return.SUCCESS;
    }
}
