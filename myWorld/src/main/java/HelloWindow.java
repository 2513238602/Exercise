// src/main/java/HelloWindow.java  （无 package）
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.vulkan.KHRPortabilityEnumeration.*;
import static org.lwjgl.vulkan.KHRPortabilitySubset.VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK12.VK_API_VERSION_1_2;

import static org.lwjgl.vulkan.VkDeviceQueueCreateInfo.nqueueCount;

public class HelloWindow {

    static class QueueFamilyIndices {
        int graphics = -1;
        int present  = -1;
        boolean complete() { return graphics >= 0 && present >= 0; }
    }

    public static void main(String[] args) {
        boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");

        // 1) GLFW 窗口
        if (!glfwInit()) throw new IllegalStateException("GLFW init failed");
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        long window = glfwCreateWindow(1280, 720, "VoxelSandbox", NULL, NULL);
        if (window == NULL) throw new RuntimeException("glfwCreateWindow failed");

        if (!GLFWVulkan.glfwVulkanSupported()) throw new IllegalStateException("GLFW reports no Vulkan support");
        PointerBuffer glfwExts = GLFWVulkan.glfwGetRequiredInstanceExtensions();
        if (glfwExts == null) throw new IllegalStateException("glfwGetRequiredInstanceExtensions returned null");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            // 2) 创建实例（合并 GLFW 所需扩展）
            VkApplicationInfo appInfo = VkApplicationInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
                    .pApplicationName(stack.UTF8("VoxelSandbox"))
                    .applicationVersion(VK_MAKE_VERSION(0,1,0))
                    .pEngineName(stack.UTF8("NoEngine"))
                    .engineVersion(VK_MAKE_VERSION(0,1,0))
                    .apiVersion(VK_API_VERSION_1_2);

            PointerBuffer exts = isMac
                    ? withPortability(stack, glfwExts)
                    : glfwExts;

            VkInstanceCreateInfo ci = VkInstanceCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                    .pApplicationInfo(appInfo)
                    .ppEnabledExtensionNames(exts);

            if (isMac) ci.flags(ci.flags() | VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR);

            PointerBuffer pInstance = stack.mallocPointer(1);
            int err = vkCreateInstance(ci, null, pInstance);
            if (err != VK_SUCCESS) throw new RuntimeException("vkCreateInstance failed: " + err);
            VkInstance instance = new VkInstance(pInstance.get(0), ci);

            // 3) 创建 Surface
            LongBuffer pSurf = stack.mallocLong(1);
            err = GLFWVulkan.glfwCreateWindowSurface(instance, window, null, pSurf);
            if (err != VK_SUCCESS) throw new RuntimeException("glfwCreateWindowSurface failed: " + err);
            long surface = pSurf.get(0);

            // 4) 选物理设备（优先离散显卡）
            VkPhysicalDevice phys = pickPhysicalDevice(stack, instance, surface, isMac);
            VkPhysicalDeviceProperties props = VkPhysicalDeviceProperties.malloc(stack);
            vkGetPhysicalDeviceProperties(phys, props);
            System.out.println("Using device: " + props.deviceNameString());

            // 5) 查队列族
            QueueFamilyIndices q = findQueueFamilies(stack, phys, surface);
            System.out.println("QueueFamilies -> graphics=" + q.graphics + ", present=" + q.present);

            // 6) 创建设备 + 队列
            VkDevice device = createLogicalDevice(stack, phys, q, isMac);
            org.lwjgl.PointerBuffer pQ = stack.mallocPointer(1); // 或上面已 import org.lwjgl.PointerBuffer;
            vkGetDeviceQueue(device, q.graphics, 0, pQ);
            long graphicsQueue = pQ.get(0);

            long presentQueue = graphicsQueue;
            if (q.present != q.graphics) {
                vkGetDeviceQueue(device, q.present, 0, pQ);
                presentQueue = pQ.get(0);
            }


            // 7) 简单消息循环（稍后我们会用这些句柄创建交换链）
            while (!glfwWindowShouldClose(window)) {
                glfwPollEvents();
            }

            // 8) 清理
            vkDeviceWaitIdle(device);
            vkDestroyDevice(device, null);
            vkDestroySurfaceKHR(instance, surface, null);
            vkDestroyInstance(instance, null);
        } finally {
            glfwDestroyWindow(window);
            glfwTerminate();
            System.out.println("Bye.");
        }
    }

    // ---- Helpers ----
    private static PointerBuffer withPortability(MemoryStack stack, PointerBuffer glfwExts) {
        PointerBuffer exts = stack.mallocPointer(glfwExts.remaining() + 1);
        exts.put(glfwExts);
        exts.put(stack.UTF8(VK_KHR_PORTABILITY_ENUMERATION_EXTENSION_NAME));
        exts.flip();
        return exts;
    }

    private static VkPhysicalDevice pickPhysicalDevice(MemoryStack stack, VkInstance instance, long surface, boolean isMac) {
        int sp = stack.getPointer();
        try {
            IntBuffer pCount = stack.mallocInt(1);
            vkEnumeratePhysicalDevices(instance, pCount, (org.lwjgl.PointerBuffer) null);
            int count = pCount.get(0);
            if (count == 0) throw new IllegalStateException("No physical devices");

            org.lwjgl.PointerBuffer pDevs = stack.mallocPointer(count);
            vkEnumeratePhysicalDevices(instance, pCount, pDevs);

            VkPhysicalDevice best = null;
            int bestScore = -1;

            for (int i = 0; i < count; i++) {
                VkPhysicalDevice dev = new VkPhysicalDevice(pDevs.get(i), instance);
                if (!isDeviceSuitable(stack, dev, surface, isMac)) continue;

                VkPhysicalDeviceProperties props = VkPhysicalDeviceProperties.malloc(stack);
                vkGetPhysicalDeviceProperties(dev, props);
                int score = (props.deviceType() == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU) ? 2 : 1;

                if (score > bestScore) { bestScore = score; best = dev; }
            }
            if (best == null) throw new IllegalStateException("No suitable device supports graphics+present+swapchain");
            return best;
        } finally {
            stack.setPointer(sp);
        }
    }


    private static boolean isDeviceSuitable(MemoryStack stack, VkPhysicalDevice dev, long surface, boolean isMac) {
        // 队列族
        QueueFamilyIndices q = findQueueFamilies(stack, dev, surface);
        if (!q.complete()) return false;

        // 设备扩展
        PointerBuffer required = isMac
                ? stack.pointers(
                stack.UTF8(VK_KHR_SWAPCHAIN_EXTENSION_NAME),
                stack.UTF8(VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME))
                : stack.pointers(stack.UTF8(VK_KHR_SWAPCHAIN_EXTENSION_NAME));
        if (!checkDeviceExtensionSupport(stack, dev, required)) return false;

        return true;
    }

    private static QueueFamilyIndices findQueueFamilies(MemoryStack stack, VkPhysicalDevice dev, long surface) {
        int sp = stack.getPointer();
        try {
            QueueFamilyIndices idx = new QueueFamilyIndices();

            IntBuffer pCount = stack.mallocInt(1);
            vkGetPhysicalDeviceQueueFamilyProperties(dev, pCount, null);
            int n = pCount.get(0);

            VkQueueFamilyProperties.Buffer props = VkQueueFamilyProperties.malloc(n, stack);
            vkGetPhysicalDeviceQueueFamilyProperties(dev, pCount, props);

            for (int i = 0; i < n; i++) {
                if ((props.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) idx.graphics = i;

                IntBuffer pSupported = stack.mallocInt(1);
                vkGetPhysicalDeviceSurfaceSupportKHR(dev, i, surface, pSupported);
                if (pSupported.get(0) == VK_TRUE) idx.present = i;

                if (idx.complete()) break;
            }
            return idx;
        } finally {
            stack.setPointer(sp);
        }
    }


    private static boolean checkDeviceExtensionSupport(MemoryStack stack, VkPhysicalDevice dev,
                                                       org.lwjgl.PointerBuffer required) {
        int sp = stack.getPointer();
        try {
            IntBuffer pCount = stack.mallocInt(1);
            vkEnumerateDeviceExtensionProperties(dev, (String) null, pCount, null);

            VkExtensionProperties.Buffer exts = VkExtensionProperties.malloc(pCount.get(0), stack);
            vkEnumerateDeviceExtensionProperties(dev, (String) null, pCount, exts);

            outer:
            for (int i = 0; i < required.remaining(); i++) {
                String need = required.getStringUTF8(i);
                for (int j = 0; j < exts.capacity(); j++) { // 用 capacity，避免 position 影响
                    if (need.equals(exts.get(j).extensionNameString())) continue outer;
                }
                return false;
            }
            return true;
        } finally {
            stack.setPointer(sp);
        }
    }


    private static VkDevice createLogicalDevice(MemoryStack stack, VkPhysicalDevice phys, QueueFamilyIndices q, boolean isMac) {
        // 唯一队列族集合
        boolean separatePresent = q.present != q.graphics;
        VkDeviceQueueCreateInfo.Buffer qInfos = VkDeviceQueueCreateInfo.calloc(separatePresent ? 2 : 1, stack);

// 第一个队列：graphics
        VkDeviceQueueCreateInfo q0 = qInfos.get(0);
        q0.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
        q0.queueFamilyIndex(q.graphics);
        q0.pQueuePriorities(stack.floats(1.0f));
        nqueueCount(q0.address(), 1); // ✅ 直接写结构体内存

        if (separatePresent) {
            VkDeviceQueueCreateInfo q1 = qInfos.get(1);
            q1.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
            q1.queueFamilyIndex(q.present);
            q1.pQueuePriorities(stack.floats(1.0f));
            nqueueCount(q1.address(), 1);
        }


        // 设备扩展
        PointerBuffer devExts = isMac
                ? stack.pointers(
                stack.UTF8(VK_KHR_SWAPCHAIN_EXTENSION_NAME),
                stack.UTF8(VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME))
                : stack.pointers(stack.UTF8(VK_KHR_SWAPCHAIN_EXTENSION_NAME));

        VkDeviceCreateInfo dci = VkDeviceCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                .pQueueCreateInfos(qInfos)
                .ppEnabledExtensionNames(devExts);
        // 这里暂不启用任何 features

        PointerBuffer pDev = stack.mallocPointer(1);
        int err = vkCreateDevice(phys, dci, null, pDev);
        if (err != VK_SUCCESS) throw new RuntimeException("vkCreateDevice failed: " + err);
        return new VkDevice(pDev.get(0), phys, dci);
    }
}
