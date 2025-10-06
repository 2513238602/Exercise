// src/main/java/HelloClear.java  （无 package）
import org.lwjgl.PointerBuffer;
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
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK12.VK_API_VERSION_1_2;
import org.lwjgl.vulkan.VkQueue;

import static org.lwjgl.vulkan.VkDeviceQueueCreateInfo.nqueueCount;




public class HelloClear {

    static class QueueFamilyIndices {
        int graphics = -1;
        int present  = -1;
        boolean complete() { return graphics >= 0 && present >= 0; }
    }

    static class SwapchainBundle {
        long swapchain;
        int imageFormat;
        VkExtent2D extent;
        LongBuffer images;
        LongBuffer imageViews;
        long renderPass;
        LongBuffer framebuffers;
        long commandPool;
        PointerBuffer commandBuffers;
        long semImageAvailable, semRenderFinished;
        long inFlightFence;
    }

    public static void main(String[] args) {
        boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");

        // 1) 窗口
        if (!glfwInit()) throw new IllegalStateException("GLFW init failed");
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        long window = glfwCreateWindow(1280, 720, "VoxelSandbox - Clear", NULL, NULL);
        if (window == NULL) throw new RuntimeException("glfwCreateWindow failed");
        if (!GLFWVulkan.glfwVulkanSupported()) throw new IllegalStateException("GLFW reports no Vulkan support");


        // ★ 保险：显式显示窗口、清除关闭标记
        glfwShowWindow(window);
        glfwFocusWindow(window);   // 让窗口获得焦点



        PointerBuffer glfwExts = GLFWVulkan.glfwGetRequiredInstanceExtensions();
        if (glfwExts == null) throw new IllegalStateException("glfwGetRequiredInstanceExtensions returned null");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            // 2) 实例
            VkApplicationInfo appInfo = VkApplicationInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
                    .pApplicationName(stack.UTF8("VoxelSandbox"))
                    .applicationVersion(VK_MAKE_VERSION(0,1,0))
                    .pEngineName(stack.UTF8("NoEngine"))
                    .engineVersion(VK_MAKE_VERSION(0,1,0))
                    .apiVersion(VK_API_VERSION_1_2);

            PointerBuffer exts = isMac ? withPortability(stack, glfwExts) : glfwExts;

            VkInstanceCreateInfo ci = VkInstanceCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                    .pApplicationInfo(appInfo)
                    .ppEnabledExtensionNames(exts);
            if (isMac) ci.flags(ci.flags() | VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR);

            PointerBuffer pInst = stack.mallocPointer(1);
            int err = vkCreateInstance(ci, null, pInst);
            if (err != VK_SUCCESS) throw new RuntimeException("vkCreateInstance failed: " + err);
            VkInstance instance = new VkInstance(pInst.get(0), ci);

            // 3) Surface
            LongBuffer pSurf = stack.mallocLong(1);
            err = GLFWVulkan.glfwCreateWindowSurface(instance, window, null, pSurf);
            if (err != VK_SUCCESS) throw new RuntimeException("glfwCreateWindowSurface failed: " + err);
            long surface = pSurf.get(0);

            // 4) 选物理设备 + 队列族
            VkPhysicalDevice phys = pickPhysicalDevice(stack, instance, surface, isMac);
            VkPhysicalDeviceProperties props = VkPhysicalDeviceProperties.malloc(stack);
            vkGetPhysicalDeviceProperties(phys, props);
            System.out.println("Using device: " + props.deviceNameString());

            QueueFamilyIndices q = findQueueFamilies(stack, phys, surface);
            System.out.println("QueueFamilies -> graphics=" + q.graphics + ", present=" + q.present);

            // 5) 逻辑设备 + 队列
            VkDevice device = createLogicalDevice(stack, phys, q, isMac);
            PointerBuffer pQ = stack.mallocPointer(1);
            vkGetDeviceQueue(device, q.graphics, 0, pQ);
            long graphicsQueue = pQ.get(0);
            long presentQueue = graphicsQueue;
            if (q.present != q.graphics) {
                vkGetDeviceQueue(device, q.present, 0, pQ);
                presentQueue = pQ.get(0);
            }

            // 6) 交换链 + 渲染通道等
            SwapchainBundle sc = createSwapchainBundle(stack, instance, device, phys, surface, q, window, isMac);

            final int MAX_FRAMES_IN_FLIGHT = 2;

            long[] imgAvailable = new long[MAX_FRAMES_IN_FLIGHT];
            long[] renderFinished = new long[MAX_FRAMES_IN_FLIGHT];
            long[] inFlightFences = new long[MAX_FRAMES_IN_FLIGHT];

            try (MemoryStack s = MemoryStack.stackPush()) {
                VkSemaphoreCreateInfo sci = VkSemaphoreCreateInfo.calloc(s)
                        .sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
                VkFenceCreateInfo fci = VkFenceCreateInfo.calloc(s)
                        .sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)
                        .flags(VK_FENCE_CREATE_SIGNALED_BIT);

                LongBuffer p = s.mallocLong(1);
                for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
                    vkCreateSemaphore(device, sci, null, p); imgAvailable[i] = p.get(0);
                    vkCreateSemaphore(device, sci, null, p); renderFinished[i] = p.get(0);
                    vkCreateFence(device, fci, null, p);     inFlightFences[i] = p.get(0);
                }
            }

            // 7) 主循环：显式 barrier + TRANSFER 清屏（为每张图跟踪布局）
            System.out.println("Entering loop...");
            int frame = 0;

// 记录每张 swapchain 图像的当前布局（初始都当作 UNDEFINED）
            int[] imageLayouts = new int[sc.images.capacity()];
            for (int i = 0; i < imageLayouts.length; i++) imageLayouts[i] = VK_IMAGE_LAYOUT_UNDEFINED;

            while (!glfwWindowShouldClose(window)) {
                glfwPollEvents();

                // 等待上一帧完成（sc.inFlightFence 在 createSwapchainBundle 里以 SIGNALED 创建）
                int wf = vkWaitForFences(device, sc.inFlightFence, true, Long.MAX_VALUE);
                if (wf != VK_SUCCESS) throw new RuntimeException("vkWaitForFences: " + wf);
                vkResetFences(device, sc.inFlightFence);

                try (MemoryStack fs = MemoryStack.stackPush()) {
                    // 1) 获取图像
                    IntBuffer pIndex = fs.mallocInt(1);
                    int acq = vkAcquireNextImageKHR(
                            device, sc.swapchain, Long.MAX_VALUE,
                            sc.semImageAvailable, VK_NULL_HANDLE, pIndex
                    );
                    if (acq != VK_SUCCESS && acq != VK_SUBOPTIMAL_KHR)
                        throw new RuntimeException("acquire: " + acq);
                    int imageIndex = pIndex.get(0);

                    // 2) 一次性命令缓冲
                    PointerBuffer pCB = fs.mallocPointer(1);
                    VkCommandBufferAllocateInfo cbai = VkCommandBufferAllocateInfo.calloc(fs)
                            .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                            .commandPool(sc.commandPool)
                            .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                            .commandBufferCount(1);
                    vkAllocateCommandBuffers(device, cbai, pCB);
                    VkCommandBuffer cb = new VkCommandBuffer(pCB.get(0), device);

                    VkCommandBufferBeginInfo bi = VkCommandBufferBeginInfo.calloc(fs)
                            .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
                            .flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
                    vkBeginCommandBuffer(cb, bi);

                    VkImageSubresourceRange range = VkImageSubresourceRange.calloc(fs)
                            .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                            .baseMipLevel(0).levelCount(1)
                            .baseArrayLayer(0).layerCount(1);

                    // ---- [Barrier 1] 旧布局 -> TRANSFER_DST_OPTIMAL ----
                    int oldLayout = imageLayouts[imageIndex];
                    int srcStage = (oldLayout == VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
                            ? VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT
                            : VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;

                    VkImageMemoryBarrier.Buffer toTransfer = VkImageMemoryBarrier.calloc(1, fs)
                            .sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
                            .srcAccessMask(0)
                            .dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT)
                            .oldLayout(oldLayout)
                            .newLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
                            .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                            .dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                            .image(sc.images.get(imageIndex))
                            .subresourceRange(range);
                    vkCmdPipelineBarrier(cb, srcStage, VK_PIPELINE_STAGE_TRANSFER_BIT, 0, null, null, toTransfer);

                    // ---- 清屏（青色）----
                    VkClearColorValue color = VkClearColorValue.calloc(fs);
                    color.float32(0, 0.0f);
                    color.float32(1, 0.6f);
                    color.float32(2, 0.6f);
                    color.float32(3, 1.0f);
                    vkCmdClearColorImage(cb, sc.images.get(imageIndex),
                            VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, color, range);

                    // ---- [Barrier 2] TRANSFER_DST_OPTIMAL -> PRESENT_SRC_KHR ----
                    VkImageMemoryBarrier.Buffer toPresent = VkImageMemoryBarrier.calloc(1, fs)
                            .sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
                            .srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT)
                            .dstAccessMask(0)
                            .oldLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
                            .newLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
                            .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                            .dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                            .image(sc.images.get(imageIndex))
                            .subresourceRange(range);
                    vkCmdPipelineBarrier(cb, VK_PIPELINE_STAGE_TRANSFER_BIT,
                            VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT, 0, null, null, toPresent);

                    vkEndCommandBuffer(cb);

                    // 3) 提交（等待 acquire 信号，提交后由 fence 通知 CPU）
                    VkSubmitInfo.Buffer submits = VkSubmitInfo.calloc(1, fs);
                    submits.get(0)
                            .sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
                            .pWaitSemaphores(fs.longs(sc.semImageAvailable))
                            .pWaitDstStageMask(fs.ints(VK_PIPELINE_STAGE_TRANSFER_BIT))
                            .pCommandBuffers(fs.pointers(cb))
                            .pSignalSemaphores(fs.longs(sc.semRenderFinished));

                    VkQueue gQueue = new VkQueue(graphicsQueue, device);
                    int sub = vkQueueSubmit(gQueue, submits, sc.inFlightFence);
                    if (sub != VK_SUCCESS) throw new RuntimeException("submit: " + sub);

                    // 4) 呈现（等待 renderFinished 信号）
                    VkPresentInfoKHR present = VkPresentInfoKHR.calloc(fs)
                            .sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
                            .pWaitSemaphores(fs.longs(sc.semRenderFinished))
                            .pSwapchains(fs.longs(sc.swapchain))
                            .pImageIndices(fs.ints(imageIndex));
                    VkQueue pQueue = (presentQueue == graphicsQueue) ? gQueue : new VkQueue(presentQueue, device);
                    int pr = vkQueuePresentKHR(pQueue, present);
                    if (pr != VK_SUCCESS && pr != VK_SUBOPTIMAL_KHR)
                        throw new RuntimeException("present: " + pr);

                    // 记录该图像的新布局
                    imageLayouts[imageIndex] = VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;

                    // 释放一次性 CB（也可以选择每帧 reset pool）
                    vkFreeCommandBuffers(device, sc.commandPool, fs.pointers(cb));
                }

                glfwSetWindowTitle(window, "VoxelSandbox - Clear  |  frame " + frame);
                frame++;
            }



            // 8) 清理
            vkDeviceWaitIdle(device);

            // 同步/命令
            vkDestroyFence(device, sc.inFlightFence, null);
            vkDestroySemaphore(device, sc.semRenderFinished, null);
            vkDestroySemaphore(device, sc.semImageAvailable, null);
            vkDestroyCommandPool(device, sc.commandPool, null);

            // 帧缓冲/渲染通道/视图/交换链
            for (int i = 0; i < sc.framebuffers.capacity(); i++) {
                vkDestroyFramebuffer(device, sc.framebuffers.get(i), null);
            }
            vkDestroyRenderPass(device, sc.renderPass, null);
            for (int i = 0; i < sc.imageViews.capacity(); i++) {
                vkDestroyImageView(device, sc.imageViews.get(i), null);
            }
            vkDestroySwapchainKHR(device, sc.swapchain, null);

            // 设备/实例
            vkDestroyDevice(device, null);
            vkDestroySurfaceKHR(instance, surface, null);
            vkDestroyInstance(instance, null);
        } finally {
            glfwDestroyWindow(window);
            glfwTerminate();
            System.out.println("Bye.");
        }
    }

    // -------------------- Helper functions --------------------

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
            vkEnumeratePhysicalDevices(instance, pCount, (PointerBuffer) null);
            int count = pCount.get(0);
            if (count == 0) throw new IllegalStateException("No physical devices");

            PointerBuffer pDevs = stack.mallocPointer(count);
            vkEnumeratePhysicalDevices(instance, pCount, pDevs);

            VkPhysicalDevice best = null;
            int bestScore = -1;

            for (int i = 0; i < count; i++) {
                VkPhysicalDevice dev = new VkPhysicalDevice(pDevs.get(i), instance);
                if (!isDeviceSuitable(stack, dev, surface, isMac)) continue;

                VkPhysicalDeviceProperties props = VkPhysicalDeviceProperties.malloc(stack);
                vkGetPhysicalDeviceProperties(dev, props);
                int score = (props.deviceType() == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU) ? 2 : 1;

                if (score > bestScore) { best = dev; bestScore = score; }
            }
            if (best == null) throw new IllegalStateException("No suitable device supports graphics+present+swapchain");
            return best;
        } finally {
            stack.setPointer(sp);
        }
    }

    private static boolean isDeviceSuitable(MemoryStack stack, VkPhysicalDevice dev, long surface, boolean isMac) {
        int sp = stack.getPointer();
        try {
            QueueFamilyIndices q = findQueueFamilies(stack, dev, surface);
            if (!q.complete()) return false;

            PointerBuffer required = isMac
                    ? stack.pointers(
                    stack.UTF8(VK_KHR_SWAPCHAIN_EXTENSION_NAME),
                    stack.UTF8(VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME))
                    : stack.pointers(stack.UTF8(VK_KHR_SWAPCHAIN_EXTENSION_NAME));
            if (!checkDeviceExtensionSupport(stack, dev, required)) return false;

            return true;
        } finally { stack.setPointer(sp); }
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

    private static boolean checkDeviceExtensionSupport(MemoryStack stack, VkPhysicalDevice dev, PointerBuffer required) {
        int sp = stack.getPointer();
        try {
            IntBuffer pCount = stack.mallocInt(1);
            vkEnumerateDeviceExtensionProperties(dev, (String) null, pCount, null);
            VkExtensionProperties.Buffer exts = VkExtensionProperties.malloc(pCount.get(0), stack);
            vkEnumerateDeviceExtensionProperties(dev, (String) null, pCount, exts);

            outer:
            for (int i = 0; i < required.remaining(); i++) {
                String need = required.getStringUTF8(i);
                for (int j = 0; j < exts.capacity(); j++) {
                    if (need.equals(exts.get(j).extensionNameString())) continue outer;
                }
                return false;
            }
            return true;
        } finally { stack.setPointer(sp); }
    }

    private static VkDevice createLogicalDevice(MemoryStack stack, VkPhysicalDevice phys, QueueFamilyIndices q, boolean isMac) {
        int sp = stack.getPointer();
        try {
            boolean separatePresent = q.present != q.graphics;
            VkDeviceQueueCreateInfo.Buffer qInfos = VkDeviceQueueCreateInfo.calloc(separatePresent ? 2 : 1, stack);

// graphics 队列
            VkDeviceQueueCreateInfo q0 = qInfos.get(0);
            q0.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
            q0.queueFamilyIndex(q.graphics);
            q0.pQueuePriorities(stack.floats(1.0f));
            nqueueCount(q0.address(), 1);           // ✅ 直接写 queueCount 字段

// present 队列（如果和 graphics 不同）
            if (separatePresent) {
                VkDeviceQueueCreateInfo q1 = qInfos.get(1);
                q1.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
                q1.queueFamilyIndex(q.present);
                q1.pQueuePriorities(stack.floats(1.0f));
                nqueueCount(q1.address(), 1);       // ✅
            }


            PointerBuffer devExts = isMac
                    ? stack.pointers(
                    stack.UTF8(VK_KHR_SWAPCHAIN_EXTENSION_NAME),
                    stack.UTF8(VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME))
                    : stack.pointers(stack.UTF8(VK_KHR_SWAPCHAIN_EXTENSION_NAME));

            VkDeviceCreateInfo dci = VkDeviceCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                    .pQueueCreateInfos(qInfos)
                    .ppEnabledExtensionNames(devExts);

            PointerBuffer pDev = stack.mallocPointer(1);
            int err = vkCreateDevice(phys, dci, null, pDev);
            if (err != VK_SUCCESS) throw new RuntimeException("vkCreateDevice failed: " + err);
            return new VkDevice(pDev.get(0), phys, dci);
        } finally { stack.setPointer(sp); }
    }

    private static SwapchainBundle createSwapchainBundle(MemoryStack stack, VkInstance instance, VkDevice device,
                                                         VkPhysicalDevice phys, long surface, QueueFamilyIndices q,
                                                         long window, boolean isMac) {
        int sp = stack.getPointer();
        try {
            // 查询能力
            VkSurfaceCapabilitiesKHR caps = VkSurfaceCapabilitiesKHR.malloc(stack);
            vkGetPhysicalDeviceSurfaceCapabilitiesKHR(phys, surface, caps);

            // 格式
            IntBuffer pCount = stack.mallocInt(1);
            vkGetPhysicalDeviceSurfaceFormatsKHR(phys, surface, pCount, null);
            VkSurfaceFormatKHR.Buffer fmts = VkSurfaceFormatKHR.malloc(pCount.get(0), stack);
            vkGetPhysicalDeviceSurfaceFormatsKHR(phys, surface, pCount, fmts);
            VkSurfaceFormatKHR fmt = chooseSurfaceFormat(fmts);
            int usage = caps.supportedUsageFlags();
            System.out.printf("Surface usage flags = 0x%08X%n", usage);

            // 如果不支持 COLOR_ATTACHMENT，就只走 ClearImage 路径
            boolean supportColorAtt = (usage & VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT) != 0;
            boolean supportTransferDst = (usage & VK_IMAGE_USAGE_TRANSFER_DST_BIT) != 0;
            if (!supportTransferDst) {
                throw new IllegalStateException("Surface does not support TRANSFER_DST for swapchain images");
            }


            // 呈现模式
            vkGetPhysicalDeviceSurfacePresentModesKHR(phys, surface, pCount, null);
            IntBuffer modes = stack.mallocInt(pCount.get(0));
            vkGetPhysicalDeviceSurfacePresentModesKHR(phys, surface, pCount, modes);
            int presentMode = choosePresentMode(modes, isMac);

            // 尺寸
            VkExtent2D extent = chooseExtent(stack, caps, window);

            // 图像数量
            int imageCount = caps.minImageCount() + 1;
            if (caps.maxImageCount() > 0 && imageCount > caps.maxImageCount()) imageCount = caps.maxImageCount();

            int imageUsage = VK_IMAGE_USAGE_TRANSFER_DST_BIT;
            if (supportColorAtt) imageUsage |= VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;

            // 创建交换链
            VkSwapchainCreateInfoKHR sci = VkSwapchainCreateInfoKHR.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
                    .surface(surface)
                    .minImageCount(imageCount)
                    .imageFormat(fmt.format())
                    .imageColorSpace(fmt.colorSpace())
                    .imageExtent(extent)
                    .imageArrayLayers(1)
                    .imageUsage(imageUsage)

                    .preTransform(caps.currentTransform())
                    .compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                    .presentMode(presentMode)
                    .clipped(true)
                    .oldSwapchain(VK_NULL_HANDLE);

            System.out.println("Format = " + fmt.format() + ", colorspace = " + fmt.colorSpace());
            System.out.println("Present mode = " + presentMode);
            System.out.println("Extent = " + extent.width() + " x " + extent.height());



            if (q.graphics != q.present) {
                sci.imageSharingMode(VK_SHARING_MODE_CONCURRENT);
                sci.pQueueFamilyIndices(stack.ints(q.graphics, q.present));
            } else {
                sci.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);
            }

            LongBuffer pSwap = stack.mallocLong(1);
            int err = vkCreateSwapchainKHR(device, sci, null, pSwap);
            if (err != VK_SUCCESS) throw new RuntimeException("vkCreateSwapchainKHR: " + err);
            long swapchain = pSwap.get(0);

            // 获取图像
            vkGetSwapchainImagesKHR(device, swapchain, pCount, null);
            LongBuffer images = stack.mallocLong(pCount.get(0));
            vkGetSwapchainImagesKHR(device, swapchain, pCount, images);

            // 图像视图
            LongBuffer views = stack.mallocLong(images.capacity());
            for (int i = 0; i < images.capacity(); i++) {
                VkImageViewCreateInfo ivci = VkImageViewCreateInfo.calloc(stack)
                        .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                        .image(images.get(i))
                        .viewType(VK_IMAGE_VIEW_TYPE_2D)
                        .format(fmt.format());
                VkComponentMapping comp = ivci.components();
                comp.r(VK_COMPONENT_SWIZZLE_IDENTITY);
                comp.g(VK_COMPONENT_SWIZZLE_IDENTITY);
                comp.b(VK_COMPONENT_SWIZZLE_IDENTITY);
                comp.a(VK_COMPONENT_SWIZZLE_IDENTITY);
                VkImageSubresourceRange sub = ivci.subresourceRange();
                sub.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
                sub.baseMipLevel(0);
                sub.levelCount(1);
                sub.baseArrayLayer(0);
                sub.layerCount(1);

                LongBuffer pView = stack.mallocLong(1);
                int rv = vkCreateImageView(device, ivci, null, pView);
                if (rv != VK_SUCCESS) throw new RuntimeException("vkCreateImageView: " + rv);
                views.put(i, pView.get(0));
            }

            // 渲染通道（一个颜色附件，CLEAR → PRESENT）
            LongBuffer pRenderPass = stack.mallocLong(1);
            long renderPass = createRenderPass(stack, device, fmt.format(), pRenderPass);

            // 帧缓冲
            LongBuffer framebuffers = stack.mallocLong(views.capacity());
            for (int i = 0; i < views.capacity(); i++) {
                VkFramebufferCreateInfo fbci = VkFramebufferCreateInfo.calloc(stack)
                        .sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
                        .renderPass(renderPass)
                        .pAttachments(stack.longs(views.get(i)))
                        .width(extent.width())
                        .height(extent.height())
                        .layers(1);
                LongBuffer pFb = stack.mallocLong(1);
                int rf = vkCreateFramebuffer(device, fbci, null, pFb);
                if (rf != VK_SUCCESS) throw new RuntimeException("vkCreateFramebuffer: " + rf);
                framebuffers.put(i, pFb.get(0));
            }

            // 命令池 & 命令缓冲（每个帧缓冲一个）
            LongBuffer pPool = stack.mallocLong(1);
            VkCommandPoolCreateInfo cpci = VkCommandPoolCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
                    .flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT)
                    .queueFamilyIndex(q.graphics);
            int cp = vkCreateCommandPool(device, cpci, null, pPool);
            if (cp != VK_SUCCESS) throw new RuntimeException("vkCreateCommandPool: " + cp);
            long commandPool = pPool.get(0);

            PointerBuffer cmdBufs = stack.mallocPointer(framebuffers.capacity());
            VkCommandBufferAllocateInfo cbai = VkCommandBufferAllocateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                    .commandPool(commandPool)
                    .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                    .commandBufferCount(framebuffers.capacity());
            int cab = vkAllocateCommandBuffers(device, cbai, cmdBufs);
            if (cab != VK_SUCCESS) throw new RuntimeException("vkAllocateCommandBuffers: " + cab);

            // 录制：把屏幕清成青色
            for (int i = 0; i < cmdBufs.capacity(); i++) {
                VkCommandBuffer cb = new VkCommandBuffer(cmdBufs.get(i), device);

                VkCommandBufferBeginInfo bi = VkCommandBufferBeginInfo.calloc(stack)
                        .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
                vkBeginCommandBuffer(cb, bi);
                // 录制命令里 —— 清屏颜色
                VkClearValue.Buffer clearValues = VkClearValue.calloc(1, stack);
                clearValues.get(0).color().float32(0, 0.0f);
                clearValues.get(0).color().float32(1, 0.6f);
                clearValues.get(0).color().float32(2, 0.6f);
                clearValues.get(0).color().float32(3, 1.0f);

                VkRenderPassBeginInfo rp = VkRenderPassBeginInfo.calloc(stack)
                        .sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
                        .renderPass(renderPass)
                        .framebuffer(framebuffers.get(i))
                        .renderArea(VkRect2D.calloc(stack)
                                .offset(VkOffset2D.calloc(stack).set(0, 0))
                                .extent(extent))
                        .pClearValues(clearValues)
                        .clearValueCount(1);  // ← 显式写上



                vkCmdBeginRenderPass(cb, rp, VK_SUBPASS_CONTENTS_INLINE);
                vkCmdEndRenderPass(cb);
                vkEndCommandBuffer(cb);
            }

            // 同步对象（一帧）
            LongBuffer pSem = stack.mallocLong(1);
            VkSemaphoreCreateInfo sciS = VkSemaphoreCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
            int s1 = vkCreateSemaphore(device, sciS, null, pSem);
            if (s1 != VK_SUCCESS) throw new RuntimeException("vkCreateSemaphore: " + s1);
            long semImageAvailable = pSem.get(0);

            int s2 = vkCreateSemaphore(device, sciS, null, pSem);
            if (s2 != VK_SUCCESS) throw new RuntimeException("vkCreateSemaphore: " + s2);
            long semRenderFinished = pSem.get(0);

            LongBuffer pFence = stack.mallocLong(1);
            VkFenceCreateInfo fci = VkFenceCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)
                    .flags(VK_FENCE_CREATE_SIGNALED_BIT);
            int fc = vkCreateFence(device, fci, null, pFence);
            if (fc != VK_SUCCESS) throw new RuntimeException("vkCreateFence: " + fc);
            long inFlightFence = pFence.get(0);

            // 打包返回
            SwapchainBundle out = new SwapchainBundle();
            out.swapchain = swapchain;
            out.imageFormat = fmt.format();
            out.extent = VkExtent2D.malloc().set(extent.width(), extent.height()); // 脱离栈保存
            out.images = LongBuffer.allocate(images.capacity());
            for (int i = 0; i < images.capacity(); i++) out.images.put(i, images.get(i));
            out.imageViews = LongBuffer.allocate(views.capacity());
            for (int i = 0; i < views.capacity(); i++) out.imageViews.put(i, views.get(i));
            out.renderPass = renderPass;
            out.framebuffers = LongBuffer.allocate(framebuffers.capacity());
            for (int i = 0; i < framebuffers.capacity(); i++) out.framebuffers.put(i, framebuffers.get(i));
            out.commandPool = commandPool;
            out.commandBuffers = PointerBuffer.allocateDirect(cmdBufs.capacity());
            for (int i = 0; i < cmdBufs.capacity(); i++) out.commandBuffers.put(i, cmdBufs.get(i));
            out.semImageAvailable = semImageAvailable;
            out.semRenderFinished = semRenderFinished;
            out.inFlightFence = inFlightFence;

            return out;
        } finally { stack.setPointer(sp); }
    }

    private static VkSurfaceFormatKHR chooseSurfaceFormat(VkSurfaceFormatKHR.Buffer formats) {
        for (int i = 0; i < formats.capacity(); i++) {
            VkSurfaceFormatKHR f = formats.get(i);
            if (f.format() == VK_FORMAT_B8G8R8A8_SRGB &&
                    f.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
                return f;
            }
        }
        return formats.get(0);
    }

    private static int choosePresentMode(IntBuffer modes, boolean isMac) {
        // macOS（MoltenVK）基本只支持 FIFO；Windows/NVIDIA 选 MAILBOX 优先
        if (!isMac) {
            for (int i = 0; i < modes.capacity(); i++) {
                if (modes.get(i) == VK_PRESENT_MODE_MAILBOX_KHR) return VK_PRESENT_MODE_MAILBOX_KHR;
            }
            for (int i = 0; i < modes.capacity(); i++) {
                if (modes.get(i) == VK_PRESENT_MODE_IMMEDIATE_KHR) return VK_PRESENT_MODE_IMMEDIATE_KHR;
            }
        }
        return VK_PRESENT_MODE_FIFO_KHR; // 必须支持
    }

    private static VkExtent2D chooseExtent(MemoryStack stack, VkSurfaceCapabilitiesKHR caps, long window) {
        if (caps.currentExtent().width() != 0xFFFFFFFF) {
            return VkExtent2D.calloc(stack).set(caps.currentExtent().width(), caps.currentExtent().height());
        } else {
            IntBuffer pw = stack.mallocInt(1);
            IntBuffer ph = stack.mallocInt(1);
            glfwGetFramebufferSize(window, pw, ph);
            int w = clamp(pw.get(0), caps.minImageExtent().width(), caps.maxImageExtent().width());
            int h = clamp(ph.get(0), caps.minImageExtent().height(), caps.maxImageExtent().height());
            return VkExtent2D.calloc(stack).set(w, h);
        }
    }

    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }

    private static long createRenderPass(MemoryStack stack, VkDevice device, int colorFormat, LongBuffer pOut) {
        int sp = stack.getPointer();
        try {
            VkAttachmentDescription.Buffer att = VkAttachmentDescription.calloc(1, stack);
            att.get(0)
                    .format(colorFormat)
                    .samples(VK_SAMPLE_COUNT_1_BIT)
                    .loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
                    .storeOp(VK_ATTACHMENT_STORE_OP_STORE)
                    .stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
                    .stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
                    .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
                    .finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

            VkAttachmentReference.Buffer colorRef = VkAttachmentReference.calloc(1, stack);
            colorRef.get(0)
                    .attachment(0)
                    .layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1, stack);
            subpass.get(0)
                    .pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
                    .colorAttachmentCount(1)
                    .pColorAttachments(colorRef);

            // 依赖：外部 → 子通道，负责布局转换
            VkSubpassDependency.Buffer dep = VkSubpassDependency.calloc(1, stack);
            dep.get(0)
                    .srcSubpass(VK_SUBPASS_EXTERNAL)
                    .dstSubpass(0)
                    .srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
                    .dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
                    .srcAccessMask(0)
                    .dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

            VkRenderPassCreateInfo rpci = VkRenderPassCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
                    .pAttachments(att)
                    .pSubpasses(subpass)
                    .pDependencies(dep);

            int r = vkCreateRenderPass(device, rpci, null, pOut);
            if (r != VK_SUCCESS) throw new RuntimeException("vkCreateRenderPass: " + r);
            return pOut.get(0);
        } finally { stack.setPointer(sp); }
    }
}
