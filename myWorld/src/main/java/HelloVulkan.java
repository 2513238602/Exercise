import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import java.nio.IntBuffer;

import static org.lwjgl.vulkan.KHRPortabilityEnumeration.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK12.VK_API_VERSION_1_2;
import org.lwjgl.PointerBuffer;


public class HelloVulkan {
    public static void main(String[] args) {
        boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkApplicationInfo appInfo = VkApplicationInfo
                    .calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
                    .pApplicationName(stack.UTF8("VoxelSandbox"))
                    .applicationVersion(VK_MAKE_VERSION(0, 1, 0))
                    .pEngineName(stack.UTF8("NoEngine"))
                    .engineVersion(VK_MAKE_VERSION(0, 1, 0))
                    .apiVersion(VK_API_VERSION_1_2);

            VkInstanceCreateInfo ci = VkInstanceCreateInfo
                    .calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                    .pApplicationInfo(appInfo);

            if (isMac) {
                PointerBuffer exts = stack.mallocPointer(1);
                exts.put(stack.UTF8(VK_KHR_PORTABILITY_ENUMERATION_EXTENSION_NAME));
                exts.flip();
                ci.ppEnabledExtensionNames(exts);
                ci.flags(ci.flags() | VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR);
            }

            PointerBuffer pInstance = stack.mallocPointer(1);
            int err = vkCreateInstance(ci, null, pInstance);
            if (err != VK_SUCCESS) throw new RuntimeException("vkCreateInstance failed: " + err);

            long instanceHandle = pInstance.get(0);
            VkInstance instance = new VkInstance(instanceHandle, ci); // 用对象包装

            IntBuffer pCount = stack.mallocInt(1);
            vkEnumeratePhysicalDevices(instance, pCount, (PointerBuffer) null);

            int count = pCount.get(0);
            if (count == 0) throw new IllegalStateException("No Vulkan physical devices found.");
            PointerBuffer pDevices = stack.mallocPointer(count);
            vkEnumeratePhysicalDevices(instance, pCount, pDevices);

            System.out.println("Detected " + count + " Vulkan device(s):");
            for (int i = 0; i < count; i++) {
                long pdHandle = pDevices.get(i);
                VkPhysicalDevice device = new VkPhysicalDevice(pdHandle, instance); // ✅ 包装成对象

                VkPhysicalDeviceProperties props = VkPhysicalDeviceProperties.malloc(stack);
                vkGetPhysicalDeviceProperties(device, props);                        // ✅ 传对象
                int api = props.apiVersion();
                System.out.println(" - " + props.deviceNameString() + " (API "
                        + VK_VERSION_MAJOR(api) + "."
                        + VK_VERSION_MINOR(api) + "."
                        + VK_VERSION_PATCH(api) + ")");
            }

            vkDestroyInstance(instance, null);
            System.out.println("OK: Vulkan instance created and destroyed.");
        }
    }
}
