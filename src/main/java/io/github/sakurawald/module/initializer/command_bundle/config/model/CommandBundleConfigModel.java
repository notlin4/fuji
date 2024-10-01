package io.github.sakurawald.module.initializer.command_bundle.config.model;

import io.github.sakurawald.core.command.structure.CommandRequirementDescriptor;
import io.github.sakurawald.module.initializer.command_bundle.structure.BundleCommandEntry;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CommandBundleConfigModel {

    List<BundleCommandEntry> entries = new ArrayList<>() {
        {
            this.add(new BundleCommandEntry(new CommandRequirementDescriptor(4, null), "my-command test-the-command-with-optional-arg <int int-arg-name> [str str-arg-name this is the default value]", List.of("say hello %player:name%", "say int is $int-arg-name", "say str is $str-arg-name")));
            this.add(new BundleCommandEntry(new CommandRequirementDescriptor(4, null), "my-command test-the-command-with-literal-arg first-literal second-literal <str str-arg-name>", List.of("say hello %player:name%", "say str is $str-arg-name")));
            this.add(new BundleCommandEntry(new CommandRequirementDescriptor(4, null), "my-command test-the-command-with-optional-arg-and-literal-arg <int int-arg-name> first-literal [str str-arg-name the default value can contains placeholder %player:name% in %world:name%]", List.of("say hello %player:name%", "say int is $int-arg-name", "say str is $str-arg-name")));
            this.add(new BundleCommandEntry(new CommandRequirementDescriptor(4, null), "my-command test-the-command-with-a-greedy-string <int int-arg-name> first-literal [greedy-string greedy-string-arg-name this is the default value]", List.of("say hello %player:name%", "say int is $int-arg-name", "say str is $greedy-string-arg-name")));
            this.add(new BundleCommandEntry(new CommandRequirementDescriptor(4, null), "give-apple-to-random-player", List.of("give %fuji:random_player% minecraft:apple %fuji:random 16 32%")));
            this.add(new BundleCommandEntry(new CommandRequirementDescriptor(4, null), "shoot <entity-type entity-type-arg-name>", List.of("execute as %player:name% run summon $entity-type-arg-name ~ ~1 ~ {ExplosionPower:4,Motion:[3.0,0.0,0.0]}")));
            this.add(new BundleCommandEntry(new CommandRequirementDescriptor(4, null), "strike", List.of("execute as %player:name% at @s run summon lightning_bolt ^ ^ ^10")));
            this.add(new BundleCommandEntry(new CommandRequirementDescriptor(0, null), "introduce-me", List.of("run as fake-op %player:name% me i am %player:name%")));
            this.add(new BundleCommandEntry(new CommandRequirementDescriptor(0, null), "rules", List.of("send-message %player:name% <rb>This is the rules of the server: <newline>blah blah...")));
            this.add(new BundleCommandEntry(new CommandRequirementDescriptor(0, null), "block-info <blockpos blockpos-arg-name>", List.of("run as fake-op %player:name% data get block $blockpos-arg-name")));
            this.add(new BundleCommandEntry(new CommandRequirementDescriptor(0, null), "entity-info <entity entity-arg-name>", List.of("run as fake-op %player:name% data get entity $entity-arg-name")));
            this.add(new BundleCommandEntry(new CommandRequirementDescriptor(0, null), "dice", List.of("say %player:name% just roll out %fuji:random 1 6% points.")));
        }
    };

}
