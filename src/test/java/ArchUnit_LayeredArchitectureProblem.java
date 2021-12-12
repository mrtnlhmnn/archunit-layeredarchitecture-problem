import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.library.Architectures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArchUnit_LayeredArchitectureProblem {

    String PKG = "mycomponent";
    JavaClasses myClasses = new ClassFileImporter().importPackages(PKG);

    @Test
    void test_layers() {
        String layerA = "LayerA";
        String layerB = "LayerB";
        String layerC = "LayerC";

        Architectures.LayeredArchitecture layeredArchitecture = Architectures.layeredArchitecture()
                .layer(layerA).definedBy(PKG+".layerA..")
                .layer(layerB).definedBy(PKG+".layerB..")
                .layer(layerC).definedBy(PKG+".layerC..")
                // ignore any dependencies to java..
                .ignoreDependency(isMyClass, isJavaClass)
                // no components to B
                .whereLayer(layerB).mayNotBeAccessedByAnyLayer()
                // B --depends--> A
                .whereLayer(layerB).mayOnlyAccessLayers(layerA)
                .whereLayer(layerA).mayOnlyBeAccessedByLayers(layerB)
                // A --depends--> C
                .whereLayer(layerC).mayOnlyBeAccessedByLayers(layerA)
/*
   Here is our problem:
   As soon as the following line is commented in, the test fails because of
       Field <mycomponent.layerB.B.a> has type <mycomponent.layerA.A> in (B.java:0)

   But why does an allowed dependency from A->C bring up an error for a dependency from B->A
   (which is even allowed)?
 */
               .whereLayer(layerA).mayOnlyAccessLayers(layerC)
        ;

        layeredArchitecture.check(myClasses);
    }

    DescribedPredicate<JavaClass> isMyClass = new DescribedPredicate<>("is in mycomponent") {
        @Override
        public boolean apply(JavaClass input) {
            return input.getPackageName().startsWith(PKG);
        }
    };

    DescribedPredicate<JavaClass> isJavaClass = new DescribedPredicate<>("is Java class") {
        @Override
        public boolean apply(JavaClass input) {
            return input.getPackageName().startsWith("java");
        }
    };

}
