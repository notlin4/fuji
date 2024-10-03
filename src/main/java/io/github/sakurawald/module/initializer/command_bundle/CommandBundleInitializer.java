package io.github.sakurawald.module.initializer.command_bundle;

import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.processor.CommandAnnotationProcessor;
import io.github.sakurawald.core.command.structure.CommandDescriptor;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.event.impl.CommandEvents;
import io.github.sakurawald.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.command_bundle.config.model.CommandBundleConfigModel;
import io.github.sakurawald.module.initializer.command_bundle.structure.BundleCommandDescriptor;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;


@CommandNode("command-bundle")
@CommandRequirement(level = 4)
public class CommandBundleInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<CommandBundleConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, CommandBundleConfigModel.class);

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // register in server started.
            registerAllBundleCommands();

            // to register bundle-commands automatically after `/reload` command.
            CommandEvents.REGISTRATION.register((a, b, c) -> registerAllBundleCommands());
        });
    }

    @Override
    public void onReload() {
        unregisterAllBundleCommands();
        registerAllBundleCommands();
    }

    @CommandNode("register")
    private static int registerAllBundleCommands() {
        LogUtil.info("register bundle commands.");

        config.getModel().getEntries().stream()
            .map(BundleCommandDescriptor::make)
            .forEach(CommandDescriptor::register);
        CommandHelper.updateCommandTree();
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("un-register")
    private static int unregisterAllBundleCommands() {
        LogUtil.info("un-register bundle commands.");

        CommandAnnotationProcessor.descriptors
            .stream()
            .filter(it -> it instanceof BundleCommandDescriptor)
            .forEach(CommandDescriptor::unregister);
        CommandHelper.updateCommandTree();
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("list")
    private static int list(@CommandSource CommandContext<ServerCommandSource> ctx) {
        CommandAnnotationProcessor.descriptors
            .stream()
            .filter(it -> it instanceof BundleCommandDescriptor)
            .forEach(it -> ctx.getSource().sendMessage(Text.literal(it.buildCommandNodePath())));
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("list-type-strings")
    private static int listTypeStrings(@CommandSource CommandContext<ServerCommandSource> ctx) {
        BaseArgumentTypeAdapter.getAdapters().forEach(adapter -> adapter.getTypeStrings().forEach(typeString -> {
            String typeClass = adapter.getTypeClasses().getFirst().getSimpleName();
            String string2types = "%s -> %s".formatted(typeString, typeClass);
            ctx.getSource().sendMessage(Text.literal(string2types));
        }));

        return CommandHelper.Return.SUCCESS;
    }

}
