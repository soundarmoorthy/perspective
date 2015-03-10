package com.freescale.sensors.sfusion;

/**
 * Created by soundararajan on 3/7/2015.
 */
public final class SampleData {

    public static int index = 0;


    public static void getNextQuaternion(FlicqQuaternion q)
    {
        if(index > 417)
            index =0;

        int ai = index++ * 7;
        float f[] = {set[ai+3], set[ai+4], set[ai+5], set[ai+6]};
        q.set(f);
        q.setXYZ(set[ai], set[ai+1], set[ai+2]);
    }

    /*
    * The data is in the following format. x, y, z, q0, q1, q2, q3
    * */
    static float set[] = {
            0.341f, -0.186f, -0.889f, 0.9796f, 0.0822f, 0.1771f, 0.0482f,
            0.341f, -0.186f, -0.889f, 0.9796f, 0.0822f, 0.1771f, 0.0482f,
            0.341f, -0.186f, -0.889f, 0.9796f, 0.0822f, 0.1771f, 0.0482f,
            0.341f, -0.186f, -0.889f, 0.9796f, 0.0822f, 0.1771f, 0.0482f,
            0.341f, -0.186f, -0.889f, 0.9796f, 0.0822f, 0.1771f, 0.0482f,
            0.341f, -0.186f, -0.889f, 0.9796f, 0.0822f, 0.1771f, 0.0482f,
            0.341f, -0.186f, -0.889f, 0.9796f, 0.0822f, 0.1771f, 0.0482f,
            0.329f, -0.187f, -0.884f, 0.9796f, 0.082f, 0.177f, 0.048f,
            0.332f, -0.172f, -0.899f, 0.9795f, 0.0825f, 0.1774f, 0.0481f,
            0.33f, -0.187f, -0.887f, 0.9793f, 0.0837f, 0.1777f, 0.0485f,
            0.339f, -0.185f, -0.891f, 0.9792f, 0.0841f, 0.1782f, 0.0487f,
            0.335f, -0.187f, -0.887f, 0.9788f, 0.0849f, 0.1794f, 0.0499f,
            0.326f, -0.179f, -0.897f, 0.9783f, 0.0859f, 0.1813f, 0.0519f,
            0.345f, -0.188f, -0.886f, 0.9776f, 0.0867f, 0.1835f, 0.0555f,
            0.333f, -0.181f, -0.893f, 0.977f, 0.0879f, 0.1846f, 0.0606f,
            0.327f, -0.151f, -0.934f, 0.9749f, 0.0943f, 0.1892f, 0.0698f,
            0.355f, -0.213f, -0.862f, 0.9719f, 0.1016f, 0.1966f, 0.0806f,
            0.385f, -0.254f, -0.853f, 0.9693f, 0.1083f, 0.2014f, 0.0904f,
            0.368f, -0.266f, -0.85f, 0.9659f, 0.1159f, 0.2087f, 0.0998f,
            0.434f, -0.329f, -0.845f, 0.9626f, 0.1265f, 0.2141f, 0.1077f,
            0.461f, -0.382f, -0.799f, 0.9595f, 0.1313f, 0.2182f, 0.1207f,
            0.345f, -0.211f, -0.925f, 0.9556f, 0.1327f, 0.2259f, 0.1348f,
            0.418f, -0.32f, -0.908f, 0.9484f, 0.1404f, 0.2386f, 0.1548f,
            0.455f, -0.433f, -0.843f, 0.9482f, 0.1316f, 0.2307f, 0.1743f,
            0.206f, -0.164f, -0.929f, 0.9486f, 0.1218f, 0.2244f, 0.187f,
            0.25f, -0.3f, -0.948f, 0.9447f, 0.1227f, 0.2253f, 0.2042f,
            0.173f, -0.248f, -0.916f, 0.9492f, 0.1095f, 0.21f, 0.2071f,
            0.109f, -0.156f, -1.04f, 0.9497f, 0.1079f, 0.2077f, 0.2078f,
            0.202f, -0.278f, -0.952f, 0.9457f, 0.117f, 0.2154f, 0.2134f,
            0.182f, -0.293f, -0.855f, 0.9506f, 0.1135f, 0.2054f, 0.203f,
            0.137f, -0.237f, -0.86f, 0.9545f, 0.1122f, 0.2037f, 0.1865f,
            0.313f, -0.237f, -0.941f, 0.9517f, 0.1209f, 0.2206f, 0.176f,
            0.433f, -0.366f, -0.858f, 0.9507f, 0.1298f, 0.2306f, 0.1618f,
            0.417f, -0.377f, -0.779f, 0.9496f, 0.1342f, 0.2384f, 0.1532f,
            0.548f, -0.395f, -0.843f, 0.9481f, 0.1406f, 0.242f, 0.1507f,
            0.39f, -0.321f, -0.839f, 0.9503f, 0.1425f, 0.2368f, 0.1434f,
            0.244f, -0.14f, -0.937f, 0.9466f, 0.1541f, 0.249f, 0.135f,
            0.477f, -0.144f, -1.044f, 0.9387f, 0.1796f, 0.2664f, 0.125f,
            0.497f, -0.398f, -0.826f, 0.9335f, 0.2005f, 0.274f, 0.1154f,
            0.603f, -0.257f, -0.915f, 0.9307f, 0.2158f, 0.2803f, 0.0929f,
            0.636f, -0.256f, -0.92f, 0.9283f, 0.2384f, 0.2786f, 0.0621f,
            0.617f, -0.443f, -0.651f, 0.9224f, 0.2613f, 0.2825f, 0.0323f,
            0.718f, -0.355f, -0.769f, 0.9192f, 0.2838f, 0.2731f, 0.0006f,
            0.588f, -0.235f, -0.722f, 0.9073f, 0.3174f, 0.274f, -0.0327f,
            0.803f, -0.495f, -0.579f, 0.8866f, 0.3652f, 0.2743f, -0.0731f,
            0.662f, -0.504f, -0.512f, 0.8738f, 0.3966f, 0.261f, -0.105f,
            0.736f, -0.704f, -0.311f, 0.8514f, 0.4438f, 0.2571f, -0.1095f,
            0.625f, -0.704f, -0.15f, 0.8365f, 0.4658f, 0.2493f, -0.1451f,
            0.591f, -0.642f, -0.367f, 0.8221f, 0.4845f, 0.2385f, -0.1804f,
            0.529f, -0.438f, -0.488f, 0.7954f, 0.5291f, 0.2365f, -0.1774f,
            0.532f, -0.655f, -0.127f, 0.7509f, 0.5933f, 0.2372f, -0.1671f,
            0.528f, -1.24f, 0.228f, 0.7439f, 0.5971f, 0.2289f, -0.1942f,
            0.407f, -0.78f, -0.03f, 0.7337f, 0.6037f, 0.2285f, -0.2124f,
            0.684f, -0.863f, 0.007f, 0.4714f, 0.705f, -0.042f, -0.5282f,
            0.691f, -0.803f, -0.128f, 0.4453f, 0.726f, -0.0525f, -0.5215f,
            0.662f, -0.884f, -0.052f, 0.4315f, 0.7379f, -0.0614f, -0.5153f,
            0.63f, -0.833f, -0.069f, 0.426f, 0.7452f, -0.0773f, -0.5071f,
            0.58f, -0.945f, 0.105f, 0.4215f, 0.7454f, -0.0902f, -0.5084f,
            0.583f, -0.94f, 0.059f, 0.4186f, 0.7421f, -0.1052f, -0.5128f,
            0.714f, -1.05f, 0.003f, 0.4291f, 0.7277f, -0.131f, -0.5189f,
            0.455f, -1.035f, -0.067f, 0.3725f, 0.7641f, -0.1787f, -0.4955f,
            0.455f, -1.035f, -0.067f, 0.3725f, 0.7641f, -0.1787f, -0.4955f,
            0.58f, -1.015f, 0.152f, 0.3341f, 0.7832f, -0.1941f, -0.4871f,
            0.572f, -0.967f, 0.003f, 0.2931f, 0.8057f, -0.2229f, -0.464f,
            0.601f, -0.635f, 0.344f, 0.2312f, 0.8371f, -0.2554f, -0.4249f,
            0.449f, -0.288f, 0.931f, 0.1685f, 0.8666f, -0.2844f, -0.3739f,
            0.394f, -0.16f, 1.096f, 0.1303f, 0.8842f, -0.2871f, -0.3447f,
            0.441f, -0.126f, 0.702f, 0.0954f, 0.9001f, -0.2887f, -0.312f,
            0.42f, 0.151f, 0.894f, 0.055f, 0.9099f, -0.2891f, -0.2925f,
            0.49f, 0.302f, 1.208f, 0.0276f, 0.9252f, -0.2649f, -0.2705f,
            0.652f, 0.445f, 1.168f, 0.0157f, 0.94f, -0.2179f, -0.2622f,
            0.706f, 0.815f, 1.121f, 0.01f, -0.9604f, 0.1448f, 0.2378f,
            0.91f, 1.307f, 1.691f, 0.0051f, 0.9755f, -0.033f, -0.2173f,
            1.174f, 2.518f, 2.325f, 0.0373f, 0.9738f, 0.1369f, -0.1779f,
            2.299f, 3.822f, 2.539f, 0.1714f, 0.904f, 0.3713f, -0.1248f,
            3.715f, 4f, 1.056f, 0.3705f, 0.7078f, 0.6004f, -0.0359f,
            4f, 4f, 0.04f, 0.5961f, 0.3725f, 0.6953f, 0.1501f,
            3.372f, 3.99f, -2.051f, 0.6794f, 0.0901f, 0.666f, 0.2946f,
            2.281f, 3.349f, -1.521f, 0.72f, -0.1214f, 0.5607f, 0.3906f,
            2.309f, 2.01f, -2.492f, 0.7155f, -0.218f, 0.4391f, 0.4978f,
            1.072f, 0.73f, -3.354f, 0.7116f, -0.2406f, 0.4057f, 0.5207f,
            1.16f, 0.857f, -1.234f, 0.7145f, -0.2306f, 0.4127f, 0.5157f,
            0.714f, 1.034f, -0.372f, 0.7176f, -0.204f, 0.4065f, 0.5275f,
            0.164f, 0.553f, -0.406f, 0.7285f, -0.2091f, 0.4137f, 0.5044f,
            0.753f, -0.062f, -0.146f, 0.7358f, -0.2215f, 0.431f, 0.473f,
            0.963f, -0.264f, -0.097f, 0.7421f, -0.2255f, 0.4295f, 0.4626f,
            0.861f, -0.268f, -0.276f, 0.7507f, -0.2246f, 0.421f, 0.4569f,
            1.036f, -0.264f, -0.302f, 0.7567f, -0.2239f, 0.4213f, 0.4469f,
            1.068f, -0.17f, -0.304f, 0.7561f, -0.231f, 0.4339f, 0.4322f,
            0.968f, -0.076f, -0.269f, 0.7537f, -0.241f, 0.444f, 0.4203f,
            1.064f, -0.075f, -0.314f, 0.7561f, -0.2406f, 0.442f, 0.4184f,
            1.07f, -0.05f, -0.408f, 0.7602f, -0.235f, 0.4448f, 0.411f,
            1.009f, 0.056f, -0.315f, 0.7638f, -0.2314f, 0.4508f, 0.3999f,
            1.008f, 0.079f, -0.254f, 0.7687f, -0.2297f, 0.4519f, 0.3902f,
            1.004f, 0.007f, -0.34f, 0.777f, -0.2238f, 0.4475f, 0.3819f,
            0.944f, -0.046f, -0.366f, 0.7853f, -0.2172f, 0.4459f, 0.3704f,
            1.015f, -0.032f, -0.279f, 0.7926f, -0.2115f, 0.4452f, 0.359f,
            1f, -0.07f, -0.327f, 0.799f, -0.2071f, 0.4434f, 0.3496f,
            0.951f, -0.079f, -0.426f, 0.8062f, -0.1991f, 0.4418f, 0.3394f,
            0.942f, 0.007f, -0.413f, 0.815f, -0.1873f, 0.4373f, 0.3309f,
            0.975f, -0.01f, -0.441f, 0.8245f, -0.173f, 0.4323f, 0.3215f,
            0.898f, 0.032f, -0.443f, 0.8324f, -0.1577f, 0.4373f, 0.3018f,
            0.946f, 0.054f, -0.277f, 0.8397f, -0.1444f, 0.4398f, 0.284f,
            0.918f, -0.034f, -0.326f, 0.8493f, -0.1307f, 0.4336f, 0.2712f,
            0.829f, -0.11f, -0.398f, 0.8581f, -0.1157f, 0.4305f, 0.2548f,
            0.892f, -0.172f, -0.342f, 0.8662f, -0.1033f, 0.4258f, 0.2402f,
            0.917f, -0.246f, -0.443f, 0.8713f, -0.0921f, 0.4247f, 0.2279f,
            0.874f, -0.218f, -0.42f, 0.8744f, -0.0829f, 0.4265f, 0.2158f,
            0.887f, -0.188f, -0.391f, 0.8772f, -0.077f, 0.4254f, 0.2088f,
            0.865f, -0.238f, -0.486f, 0.8802f, -0.0723f, 0.4223f, 0.2042f,
            0.834f, -0.137f, -0.514f, 0.8841f, -0.0666f, 0.4173f, 0.1994f,
            0.849f, -0.102f, -0.458f, 0.8882f, -0.0635f, 0.4123f, 0.1927f,
            0.872f, -0.143f, -0.522f, 0.8947f, -0.0612f, 0.3998f, 0.1896f,
            0.765f, -0.171f, -0.586f, 0.8992f, -0.0565f, 0.3932f, 0.1833f,
            0.823f, -0.16f, -0.548f, 0.9005f, -0.0523f, 0.3965f, 0.1708f,
            0.836f, -0.193f, -0.536f, 0.9013f, -0.0486f, 0.3994f, 0.1604f,
            0.875f, -0.217f, -0.539f, 0.8992f, -0.0442f, 0.4093f, 0.1484f,
            0.938f, -0.18f, -0.477f, 0.8986f, -0.0417f, 0.4135f, 0.1407f,
            0.908f, -0.156f, -0.508f, 0.9002f, -0.0406f, 0.4109f, 0.1381f,
            0.85f, -0.163f, -0.565f, 0.9009f, -0.0383f, 0.4103f, 0.1362f,
            0.813f, -0.126f, -0.551f, 0.9007f, -0.0368f, 0.412f, 0.133f,
            0.805f, -0.131f, -0.52f, 0.9012f, -0.0358f, 0.4119f, 0.1297f,
            0.821f, -0.153f, -0.551f, 0.9012f, -0.0352f, 0.4132f, 0.1262f,
            0.854f, -0.124f, -0.526f, 0.8999f, -0.0355f, 0.4172f, 0.122f,
            0.855f, -0.134f, -0.517f, 0.9001f, -0.0368f, 0.417f, 0.1205f,
            0.851f, -0.141f, -0.545f, 0.8994f, -0.0366f, 0.4194f, 0.1179f,
            0.857f, -0.106f, -0.514f, 0.8979f, -0.0365f, 0.4232f, 0.116f,
            0.841f, -0.104f, -0.5f, 0.897f, -0.0361f, 0.4256f, 0.1141f,
            0.864f, -0.107f, -0.497f, 0.8967f, -0.0359f, 0.4266f, 0.113f,
            0.842f, -0.104f, -0.49f, 0.8959f, -0.0367f, 0.4287f, 0.1109f,
            0.854f, -0.121f, -0.53f, 0.8956f, -0.0376f, 0.4299f, 0.1083f,
            0.892f, -0.116f, -0.522f, 0.8952f, -0.0372f, 0.4311f, 0.1071f,
            0.875f, -0.122f, -0.519f, 0.8947f, -0.0361f, 0.4324f, 0.1064f,
            0.862f, -0.119f, -0.497f, 0.8936f, -0.0348f, 0.4347f, 0.1061f,
            0.863f, -0.122f, -0.479f, 0.8931f, -0.0347f, 0.4356f, 0.1073f,
            0.829f, -0.118f, -0.512f, 0.8926f, -0.0346f, 0.4364f, 0.1075f,
            0.847f, -0.122f, -0.518f, 0.8924f, -0.0346f, 0.4368f, 0.1081f,
            0.845f, -0.122f, -0.54f, 0.8921f, -0.034f, 0.4374f, 0.1081f,
            0.826f, -0.127f, -0.538f, 0.8921f, -0.0323f, 0.4379f, 0.1067f,
            0.854f, -0.124f, -0.517f, 0.892f, -0.0306f, 0.4386f, 0.1055f,
            0.86f, -0.122f, -0.501f, 0.8911f, -0.0288f, 0.4406f, 0.1047f,
            0.851f, -0.143f, -0.517f, 0.8909f, -0.026f, 0.4415f, 0.1036f,
            0.859f, -0.141f, -0.488f, 0.8907f, -0.0236f, 0.4422f, 0.1031f,
            0.854f, -0.151f, -0.49f, 0.8901f, -0.0216f, 0.4434f, 0.1033f,
            0.822f, -0.174f, -0.52f, 0.8897f, -0.0194f, 0.4443f, 0.1028f,
            0.839f, -0.175f, -0.509f, 0.8891f, -0.0182f, 0.4458f, 0.1024f,
            0.848f, -0.165f, -0.497f, 0.8882f, -0.0171f, 0.4475f, 0.1024f,
            0.85f, -0.163f, -0.522f, 0.8882f, -0.0157f, 0.4476f, 0.1026f,
            0.842f, -0.158f, -0.528f, 0.8872f, -0.0144f, 0.4497f, 0.1019f,
            0.83f, -0.139f, -0.515f, 0.8874f, -0.0139f, 0.4493f, 0.1018f,
            0.824f, -0.115f, -0.578f, 0.8895f, -0.0124f, 0.4456f, 0.1007f,
            0.796f, -0.087f, -0.606f, 0.8924f, -0.009f, 0.4408f, 0.0963f,
            0.754f, 0.016f, -0.602f, 0.8973f, -0.005f, 0.4325f, 0.088f,
            0.708f, 0.042f, -0.573f, 0.9042f, -0.0002f, 0.4199f, 0.0779f,
            0.746f, -0.013f, -0.622f, 0.909f, 0.0043f, 0.4121f, 0.062f,
            0.756f, 0.036f, -0.633f, 0.914f, 0.0107f, 0.4032f, 0.0438f,
            0.743f, 0.065f, -0.652f, 0.9216f, 0.0169f, 0.3871f, 0.0243f,
            0.72f, 0.053f, -0.716f, 0.9275f, 0.0239f, 0.3731f, -0.0002f,
            0.749f, 0.035f, -0.77f, 0.9312f, 0.0359f, 0.3612f, -0.0325f,
            0.837f, 0.019f, -0.781f, 0.936f, 0.0516f, 0.3409f, -0.0711f,
            0.944f, -0.059f, -0.829f, 0.9441f, 0.0689f, 0.3037f, -0.1086f,
            0.943f, -0.055f, -0.898f, 0.944f, 0.0945f, 0.2762f, -0.1538f,
            0.942f, -0.104f, -0.901f, 0.9396f, 0.1269f, 0.2532f, -0.1921f,
            0.762f, -0.104f, -0.874f, 0.9347f, 0.1631f, 0.2195f, -0.2268f,
            0.687f, -0.208f, -0.887f, 0.9316f, 0.1979f, 0.1592f, -0.2602f,
            0.721f, -0.344f, -0.85f, 0.9353f, 0.2108f, 0.0551f, -0.2787f,
            0.377f, -0.391f, -0.787f, 0.9316f, 0.2071f, -0.0682f, -0.2909f,
            0.102f, -0.569f, -0.875f, 0.9144f, 0.1953f, -0.201f, -0.2921f,
            -0.323f, -0.471f, -0.583f, 0.8871f, 0.1817f, -0.3006f, -0.2993f,
            -0.411f, -0.631f, -0.42f, 0.8507f, 0.1759f, -0.3712f, -0.328f,
            -0.528f, -0.963f, -0.582f, 0.8029f, 0.1583f, -0.4581f, -0.347f,
            -0.38f, -0.928f, -0.865f, 0.7449f, 0.1648f, -0.545f, -0.3478f,
            -0.598f, -0.629f, -0.669f, 0.6743f, 0.2291f, -0.619f, -0.3311f,
            -0.31f, -0.266f, -0.089f, 0.5622f, 0.3254f, -0.6992f, -0.2987f,
            -0.116f, -0.172f, 0.958f, 0.444f, 0.4363f, -0.7322f, -0.2763f,
            -0.017f, 0.156f, 1.355f, 0.3458f, 0.5982f, -0.6789f, -0.2484f,
            0.747f, 0.629f, 1.296f, 0.2332f, 0.7554f, -0.5703f, -0.2232f,
            1.262f, 0.844f, 2.223f, 0.1465f, 0.8707f, -0.4316f, -0.1848f,
            1.489f, 0.743f, 2.062f, 0.1405f, 0.9398f, -0.2657f, -0.1626f,
            2.198f, 0.567f, 1.696f, 0.1467f, 0.9606f, -0.1799f, -0.1526f,
            1.467f, 1.041f, 2.354f, 0.2232f, 0.9664f, -0.0645f, -0.1104f,
            1.529f, 1.548f, 1.661f, 0.3288f, 0.9319f, 0.1485f, -0.037f,
            2.318f, 2.463f, 2.056f, 0.4689f, 0.803f, 0.3675f, 0.0168f,
            3.57f, 3.64f, 0.068f, 0.6428f, 0.5492f, 0.5231f, 0.108f,
            3.01f, 3.4f, -1.243f, 0.7501f, 0.249f, 0.5918f, 0.1588f,
            3.544f, 1.715f, -3.127f, 0.8083f, 0.0866f, 0.5246f, 0.2529f,
            1.908f, 0.783f, -2.865f, 0.8103f, 0.0112f, 0.5028f, 0.3008f,
            1.438f, 0.501f, -2.347f, 0.803f, -0.0244f, 0.4939f, 0.3327f,
            0.58f, 0.48f, -1.716f, 0.8158f, -0.0234f, 0.4647f, 0.3433f,
            0.242f, 0.304f, -0.782f, 0.8307f, -0.0231f, 0.4485f, 0.3289f,
            0.771f, -0.045f, -0.383f, 0.8299f, -0.0268f, 0.4652f, 0.3069f,
            0.821f, -0.15f, -0.249f, 0.8288f, -0.0329f, 0.4776f, 0.2896f,
            1.003f, -0.222f, -0.252f, 0.8351f, -0.0428f, 0.4672f, 0.2872f,
            0.919f, -0.295f, -0.545f, 0.8339f, -0.0519f, 0.4684f, 0.2872f,
            0.817f, -0.246f, -0.673f, 0.8323f, -0.0565f, 0.4747f, 0.2805f,
            0.941f, -0.152f, -0.451f, 0.8342f, -0.0621f, 0.4704f, 0.2811f,
            0.884f, -0.15f, -0.547f, 0.8349f, -0.0713f, 0.4658f, 0.2845f,
            0.769f, -0.102f, -0.636f, 0.8382f, -0.077f, 0.4598f, 0.2832f,
            0.874f, -0.04f, -0.473f, 0.8437f, -0.0847f, 0.4455f, 0.2874f,
            0.777f, -0.066f, -0.67f, 0.8506f, -0.0918f, 0.4296f, 0.2889f,
            0.609f, -0.047f, -0.698f, 0.8521f, -0.0901f, 0.4336f, 0.2788f,
            0.745f, -0.006f, -0.491f, 0.8558f, -0.0889f, 0.4327f, 0.2694f,
            0.678f, -0.096f, -0.63f, 0.8611f, -0.0884f, 0.4289f, 0.2582f,
            0.719f, -0.114f, -0.701f, 0.8645f, -0.0859f, 0.4328f, 0.2406f,
            0.848f, -0.103f, -0.64f, 0.8693f, -0.083f, 0.4314f, 0.2265f,
            0.828f, -0.097f, -0.666f, 0.8734f, -0.079f, 0.4308f, 0.213f,
            0.808f, -0.118f, -0.661f, 0.8742f, -0.0734f, 0.4383f, 0.1958f,
            0.815f, -0.062f, -0.563f, 0.8777f, -0.0698f, 0.4374f, 0.1828f,
            0.77f, -0.071f, -0.606f, 0.8855f, -0.0665f, 0.4257f, 0.174f,
            0.708f, -0.079f, -0.675f, 0.8896f, -0.0633f, 0.4228f, 0.1609f,
            0.827f, -0.041f, -0.559f, 0.8921f, -0.0601f, 0.4209f, 0.1532f,
            0.787f, -0.101f, -0.665f, 0.895f, -0.057f, 0.4184f, 0.144f,
            0.782f, -0.076f, -0.636f, 0.8932f, -0.0523f, 0.4282f, 0.1268f,
            0.776f, -0.066f, -0.579f, 0.8942f, -0.048f, 0.4308f, 0.1119f,
            0.801f, -0.151f, -0.619f, 0.8956f, -0.0412f, 0.4325f, 0.0958f,
            0.798f, -0.149f, -0.581f, 0.8953f, -0.0344f, 0.4372f, 0.0783f,
            0.817f, -0.177f, -0.587f, 0.899f, -0.0282f, 0.4323f, 0.0648f,
            0.811f, -0.168f, -0.647f, 0.9021f, -0.0212f, 0.428f, 0.0505f,
            0.768f, -0.135f, -0.601f, 0.9031f, -0.0132f, 0.4276f, 0.0361f,
            0.771f, -0.171f, -0.577f, 0.9046f, -0.0064f, 0.4256f, 0.0223f,
            0.81f, -0.172f, -0.614f, 0.9054f, -0.0004f, 0.4245f, 0.0089f,
            0.816f, -0.182f, -0.556f, 0.9046f, 0.007f, 0.4263f, -0.0039f,
            0.802f, -0.217f, -0.567f, 0.9016f, 0.0158f, 0.4319f, -0.0156f,
            0.804f, -0.242f, -0.593f, 0.9007f, 0.0259f, 0.4329f, -0.026f,
            0.792f, -0.239f, -0.534f, 0.8975f, 0.0369f, 0.4378f, -0.0376f,
            0.83f, -0.249f, -0.559f, 0.8967f, 0.0462f, 0.4379f, -0.0454f,
            0.779f, -0.219f, -0.577f, 0.8937f, 0.0546f, 0.4421f, -0.0527f,
            0.778f, -0.199f, -0.547f, 0.8926f, 0.0599f, 0.4432f, -0.0579f,
            0.79f, -0.207f, -0.597f, 0.8918f, 0.0641f, 0.4434f, -0.0634f,
            0.784f, -0.179f, -0.565f, 0.8912f, 0.0681f, 0.4431f, -0.0692f,
            0.78f, -0.182f, -0.543f, 0.8906f, 0.0713f, 0.4428f, -0.0755f,
            0.782f, -0.198f, -0.562f, 0.8893f, 0.0752f, 0.4433f, -0.0832f,
            0.771f, -0.184f, -0.522f, 0.8875f, 0.0792f, 0.4446f, -0.0912f,
            0.767f, -0.212f, -0.532f, 0.8852f, 0.0834f, 0.4469f, -0.0991f,
            0.786f, -0.232f, -0.553f, 0.8823f, 0.0887f, 0.4493f, -0.1084f,
            0.811f, -0.207f, -0.523f, 0.8795f, 0.093f, 0.4516f, -0.1174f,
            0.815f, -0.206f, -0.563f, 0.8767f, 0.099f, 0.4533f, -0.1272f,
            0.819f, -0.198f, -0.53f, 0.8735f, 0.1052f, 0.4548f, -0.1379f,
            0.841f, -0.212f, -0.497f, 0.8706f, 0.1111f, 0.4558f, -0.1483f,
            0.849f, -0.234f, -0.51f, 0.8658f, 0.1189f, 0.4592f, -0.1592f,
            0.861f, -0.24f, -0.463f, 0.8616f, 0.1255f, 0.4617f, -0.1698f,
            0.876f, -0.259f, -0.467f, 0.8576f, 0.1319f, 0.4637f, -0.1793f,
            0.867f, -0.264f, -0.503f, 0.8531f, 0.1397f, 0.4658f, -0.1889f,
            0.88f, -0.262f, -0.454f, 0.849f, 0.1468f, 0.4669f, -0.199f,
            0.918f, -0.271f, -0.429f, 0.8464f, 0.153f, 0.466f, -0.2075f,
            0.9f, -0.286f, -0.454f, 0.8442f, 0.1596f, 0.4642f, -0.2155f,
            0.883f, -0.286f, -0.42f, 0.842f, 0.1652f, 0.4627f, -0.2228f,
            0.905f, -0.3f, -0.429f, 0.8413f, 0.1695f, 0.46f, -0.2278f,
            0.876f, -0.3f, -0.485f, 0.8411f, 0.1748f, 0.4555f, -0.2336f,
            0.851f, -0.28f, -0.446f, 0.8396f, 0.1798f, 0.4531f, -0.2396f,
            0.849f, -0.284f, -0.416f, 0.8378f, 0.1836f, 0.4525f, -0.2443f,
            0.83f, -0.294f, -0.441f, 0.8368f, 0.1869f, 0.4507f, -0.2483f,
            0.831f, -0.303f, -0.438f, 0.8355f, 0.1903f, 0.4491f, -0.253f,
            0.862f, -0.3f, -0.41f, 0.8337f, 0.1937f, 0.4487f, -0.2572f,
            0.858f, -0.306f, -0.422f, 0.8327f, 0.1956f, 0.4477f, -0.2605f,
            0.857f, -0.314f, -0.462f, 0.8324f, 0.1971f, 0.4453f, -0.2645f,
            0.853f, -0.305f, -0.438f, 0.8307f, 0.1993f, 0.4454f, -0.2679f,
            0.848f, -0.3f, -0.444f, 0.8305f, 0.2f, 0.4439f, -0.2706f,
            0.835f, -0.3f, -0.483f, 0.8301f, 0.201f, 0.4423f, -0.2738f,
            0.85f, -0.294f, -0.476f, 0.8286f, 0.2027f, 0.442f, -0.2773f,
            0.855f, -0.294f, -0.498f, 0.8272f, 0.2054f, 0.4413f, -0.2806f,
            0.834f, -0.296f, -0.535f, 0.8273f, 0.2087f, 0.4361f, -0.2859f,
            0.84f, -0.293f, -0.542f, 0.8247f, 0.2154f, 0.4325f, -0.2938f,
            0.846f, -0.307f, -0.57f, 0.8238f, 0.2231f, 0.4245f, -0.3022f,
            0.936f, -0.284f, -0.636f, 0.8258f, 0.2288f, 0.4074f, -0.3158f,
            0.998f, -0.228f, -0.694f, 0.8285f, 0.2355f, 0.3863f, -0.3301f,
            0.926f, -0.159f, -0.792f, 0.8281f, 0.2448f, 0.3666f, -0.3462f,
            0.983f, -0.135f, -0.934f, 0.8238f, 0.259f, 0.3363f, -0.3758f,
            1.023f, -0.043f, -0.99f, 0.8245f, 0.2772f, 0.2872f, -0.401f,
            0.983f, -0.138f, -0.794f, 0.8245f, 0.3122f, 0.2149f, -0.4202f,
            1.087f, -0.397f, -0.757f, 0.8472f, 0.3019f, 0.0783f, -0.4301f,
            0.684f, -0.45f, -0.79f, 0.8607f, 0.2733f, -0.0946f, -0.4189f,
            0.288f, -0.61f, -0.454f, 0.8563f, 0.2332f, -0.2636f, -0.3779f,
            -0.388f, -0.695f, -0.48f, 0.8243f, 0.1863f, -0.4209f, -0.3297f,
            -1.257f, -0.746f, -0.323f, 0.7474f, 0.171f, -0.5584f, -0.3168f,
            -1.098f, -0.459f, -0.182f, 0.6497f, 0.2337f, -0.6457f, -0.326f,
            -0.595f, 0.208f, -0.038f, 0.5301f, 0.3857f, -0.6777f, -0.3331f,
            0.093f, 1.051f, 0.927f, 0.3894f, 0.621f, -0.6106f, -0.2998f,
            0.836f, 1.388f, 1.869f, 0.2292f, 0.8092f, -0.4796f, -0.2502f,
            2.121f, 1.386f, 2.528f, 0.1177f, 0.8902f, -0.384f, -0.2151f,
            1.685f, 1.728f, 3.474f, 0.1257f, 0.9305f, -0.3035f, -0.1621f,
            1.256f, 2.09f, 2.786f, 0.1945f, 0.9594f, -0.1682f, -0.1158f,
            1.792f, 2.851f, 3.034f, 0.3071f, 0.9467f, 0.0561f, -0.079f,
            3.203f, 3.971f, 2.645f, 0.5254f, 0.7711f, 0.3593f, -0.0142f,
            4f, 4f, -0.118f, 0.7315f, 0.4229f, 0.5301f, 0.0709f,
            3.858f, 4f, -1.686f, 0.7978f, 0.1072f, 0.5749f, 0.1467f,
            3.384f, 3.316f, -2.5f, 0.8018f, -0.1005f, 0.5509f, 0.2086f,
            2.456f, 2.031f, -3.317f, 0.8012f, -0.1896f, 0.4977f, 0.2729f,
            1.239f, 1.236f, -2.875f, 0.783f, -0.212f, 0.5107f, 0.285f,
            0.748f, 0.621f, -1.772f, 0.7775f, -0.1832f, 0.5474f, 0.2493f,
            0.791f, 0.363f, -0.552f, 0.8019f, -0.1325f, 0.5433f, 0.2102f,
            1.101f, 0.032f, 0.214f, 0.819f, -0.1073f, 0.5353f, 0.1764f,
            1.414f, -0.228f, 0.116f, 0.8207f, -0.1013f, 0.5405f, 0.1548f,
            1.27f, -0.412f, -0.276f, 0.8343f, -0.0927f, 0.5212f, 0.1541f,
            1.183f, -0.39f, -0.59f, 0.8672f, -0.0723f, 0.4645f, 0.1641f,
            0.981f, -0.342f, -0.647f, 0.8854f, -0.0472f, 0.43f, 0.1701f,
            0.725f, -0.256f, -0.69f, 0.9069f, -0.0175f, 0.3829f, 0.1752f,
            0.593f, -0.338f, -0.806f, 0.9417f, 0.0269f, 0.2842f, 0.1782f,
            0.559f, -0.238f, -0.64f, 0.9604f, 0.0573f, 0.2053f, 0.1795f,
            0.268f, -0.151f, -0.606f, 0.9676f, 0.0798f, 0.1687f, 0.17f,
            0.248f, -0.207f, -0.754f, 0.9753f, 0.1039f, 0.1197f, 0.1541f,
            0.237f, -0.114f, -0.524f, 0.9791f, 0.1163f, 0.0918f, 0.1393f,
            0.148f, 0.025f, -0.518f, 0.9851f, 0.109f, 0.0647f, 0.1159f,
            0.224f, -0.014f, -0.805f, 0.9895f, 0.0983f, 0.0335f, 0.1001f,
            0.054f, 0.114f, -0.789f, 0.9916f, 0.0919f, 0.0301f, 0.0857f,
            0.146f, 0.138f, -0.814f, 0.9943f, 0.078f, 0.0232f, 0.0689f,
            0.176f, 0.051f, -1.34f, 0.9944f, 0.082f, 0.0298f, 0.0602f,
            0.111f, -0.032f, -1.171f, 0.9934f, 0.0928f, 0.0421f, 0.0534f,
            0.02f, -0.078f, -0.781f, 0.9927f, 0.0989f, 0.0565f, 0.0388f,
            0.017f, -0.166f, -0.871f, 0.9943f, 0.0897f, 0.0498f, 0.029f,
            0.05f, -0.157f, -0.965f, 0.995f, 0.0826f, 0.0503f, 0.0228f,
            0.173f, 0.018f, -1.2f, 0.9981f, 0.0424f, 0.039f, 0.0196f,
            0.051f, 0.025f, -1.295f, 0.9982f, 0.0447f, 0.0375f, 0.0154f,
            0.118f, -0.109f, -0.862f, 0.9978f, 0.0538f, 0.0346f, 0.0149f,
            -0.001f, -0.162f, -0.808f, 0.9986f, 0.0427f, 0.0302f, 0.01f,
            0.032f, -0.061f, -0.972f, 0.9986f, 0.0445f, 0.0288f, 0.0071f,
            0.032f, -0.072f, -0.948f, 0.9985f, 0.0466f, 0.0268f, 0.0043f,
            0.036f, -0.072f, -0.948f, 0.9985f, 0.0481f, 0.0248f, 0.0019f,
            0.036f, -0.072f, -0.948f, 0.9985f, 0.0499f, 0.0229f, -0.0004f,
            0.036f, -0.073f, -0.948f, 0.9984f, 0.052f, 0.021f, -0.003f,
            0.036f, -0.073f, -0.95f, 0.9984f, 0.0535f, 0.0192f, -0.0052f,
            0.037f, -0.072f, -0.947f, 0.9983f, 0.0556f, 0.0174f, -0.0075f,
            0.036f, -0.073f, -0.949f, 0.9982f, 0.0573f, 0.0157f, -0.0094f,
            0.037f, -0.073f, -0.948f, 0.9981f, 0.0587f, 0.0139f, -0.0114f,
            0.036f, -0.072f, -0.947f, 0.998f, 0.0604f, 0.0121f, -0.0134f,
            0.036f, -0.073f, -0.948f, 0.9979f, 0.062f, 0.0103f, -0.0151f,
            0.037f, -0.072f, -0.948f, 0.9978f, 0.0632f, 0.0086f, -0.0167f,
            0.036f, -0.073f, -0.948f, 0.9977f, 0.0653f, 0.0067f, -0.0183f,
            0.036f, -0.073f, -0.948f, 0.9976f, 0.0668f, 0.0049f, -0.0197f,
            0.036f, -0.072f, -0.945f, 0.9974f, 0.0683f, 0.0032f, -0.0212f,
            0.036f, -0.073f, -0.953f, 0.9973f, 0.0699f, 0.0014f, -0.0227f,
            0.035f, -0.072f, -0.944f, 0.9971f, 0.0718f, -0.0002f, -0.0239f,
            0.036f, -0.073f, -0.948f, 0.997f, 0.0732f, -0.0016f, -0.0248f,
            0.037f, -0.073f, -0.947f, 0.9969f, 0.0747f, -0.0029f, -0.0262f,
            0.036f, -0.073f, -0.95f, 0.9967f, 0.076f, -0.0042f, -0.0272f,
            0.035f, -0.072f, -0.946f, 0.9966f, 0.0771f, -0.0058f, -0.0283f,
            0.035f, -0.073f, -0.95f, 0.9964f, 0.0787f, -0.0073f, -0.0295f,
            0.038f, -0.072f, -0.948f, 0.9963f, 0.0803f, -0.0088f, -0.0304f,
            0.036f, -0.073f, -0.948f, 0.9961f, 0.0814f, -0.0101f, -0.0313f,
            0.036f, -0.072f, -0.947f, 0.996f, 0.0827f, -0.0117f, -0.0319f,
            0.036f, -0.073f, -0.949f, 0.9958f, 0.0842f, -0.0132f, -0.0325f,
            0.036f, -0.072f, -0.948f, 0.9957f, 0.0851f, -0.0145f, -0.0332f,
            0.036f, -0.073f, -0.947f, 0.9956f, 0.0864f, -0.0158f, -0.034f,
            0.035f, -0.073f, -0.947f, 0.9954f, 0.0877f, -0.017f, -0.0346f,
            0.036f, -0.073f, -0.948f, 0.9953f, 0.0889f, -0.0183f, -0.035f,
            0.037f, -0.073f, -0.948f, 0.9951f, 0.0899f, -0.0196f, -0.0355f,
            0.036f, -0.072f, -0.947f, 0.995f, 0.0912f, -0.0209f, -0.0356f,
            0.037f, -0.073f, -0.948f, 0.9948f, 0.0921f, -0.0222f, -0.0362f,
            0.036f, -0.073f, -0.948f, 0.9947f, 0.0929f, -0.0235f, -0.0363f,
            0.036f, -0.072f, -0.948f, 0.9945f, 0.0943f, -0.0249f, -0.0369f,
            0.036f, -0.073f, -0.946f, 0.9944f, 0.0954f, -0.0262f, -0.0368f,
            0.036f, -0.073f, -0.949f, 0.9943f, 0.0966f, -0.0274f, -0.0369f,
            0.036f, -0.072f, -0.947f, 0.9942f, 0.0974f, -0.0286f, -0.0366f,
            0.035f, -0.073f, -0.948f, 0.9941f, 0.0982f, -0.0297f, -0.0363f,
            0.037f, -0.072f, -0.949f, 0.9939f, 0.0992f, -0.0307f, -0.0363f,
            0.037f, -0.073f, -0.947f, 0.9938f, 0.1001f, -0.0318f, -0.036f,
            0.036f, -0.073f, -0.948f, 0.9937f, 0.1007f, -0.0331f, -0.0357f,
            0.036f, -0.073f, -0.947f, 0.9936f, 0.102f, -0.0344f, -0.0356f,
            0.036f, -0.073f, -0.946f, 0.9934f, 0.1029f, -0.0354f, -0.0353f,
            0.037f, -0.073f, -0.949f, 0.9933f, 0.1037f, -0.0364f, -0.0353f,
            0.036f, -0.073f, -0.948f, 0.9932f, 0.1047f, -0.0372f, -0.0346f,
            0.035f, -0.072f, -0.948f, 0.9931f, 0.1054f, -0.0382f, -0.0341f,
            0.036f, -0.073f, -0.949f, 0.993f, 0.1058f, -0.0391f, -0.0338f,
            0.036f, -0.072f, -0.949f, 0.9929f, 0.1068f, -0.0401f, -0.0335f,
            0.036f, -0.072f, -0.948f, 0.9928f, 0.1077f, -0.041f, -0.0331f,
            0.036f, -0.072f, -0.947f, 0.9927f, 0.1083f, -0.0417f, -0.0328f,
            0.036f, -0.073f, -0.948f, 0.9926f, 0.1091f, -0.0426f, -0.0321f,
            0.036f, -0.073f, -0.949f, 0.9925f, 0.1098f, -0.0434f, -0.0314f,
            0.035f, -0.073f, -0.95f, 0.9924f, 0.1105f, -0.0443f, -0.0308f,
            0.036f, -0.073f, -0.948f, 0.9924f, 0.1105f, -0.045f, -0.0298f,
            0.036f, -0.072f, -0.95f, 0.9923f, 0.1112f, -0.0459f, -0.0291f,
            0.037f, -0.073f, -0.947f, 0.9922f, 0.1118f, -0.0467f, -0.0281f,
            0.036f, -0.072f, -0.949f, 0.9921f, 0.1124f, -0.0475f, -0.0276f,
            0.035f, -0.072f, -0.947f, 0.9921f, 0.1131f, -0.0482f, -0.0267f,
            0.037f, -0.073f, -0.95f, 0.992f, 0.1135f, -0.0488f, -0.0259f,
            0.036f, -0.072f, -0.948f, 0.9919f, 0.1141f, -0.0493f, -0.0256f,
            0.037f, -0.072f, -0.949f, 0.9919f, 0.1145f, -0.0499f, -0.0244f,
            0.036f, -0.072f, -0.948f, 0.9918f, 0.1148f, -0.0504f, -0.0234f,
            0.036f, -0.073f, -0.949f, 0.9918f, 0.1154f, -0.0509f, -0.0227f,
            0.037f, -0.073f, -0.949f, 0.9917f, 0.1156f, -0.0515f, -0.022f,
            0.037f, -0.073f, -0.95f, 0.9917f, 0.1159f, -0.052f, -0.0209f,
            0.036f, -0.072f, -0.948f, 0.9916f, 0.1163f, -0.0524f, -0.0198f,
            0.037f, -0.072f, -0.948f, 0.9916f, 0.1168f, -0.0531f, -0.0188f,
            0.036f, -0.072f, -0.948f, 0.9915f, 0.1172f, -0.0535f, -0.0178f,
            0.037f, -0.072f, -0.949f, 0.9915f, 0.1176f, -0.054f, -0.0166f,
            0.036f, -0.072f, -0.949f, 0.9914f, 0.118f, -0.0544f, -0.0154f,
            0.035f, -0.072f, -0.948f, 0.9914f, 0.1182f, -0.0546f, -0.0142f,
            0.035f, -0.072f, -0.95f, 0.9914f, 0.1183f, -0.0549f, -0.0132f,
            0.036f, -0.073f, -0.95f, 0.9913f, 0.1187f, -0.0553f, -0.0121f,
            0.036f, -0.072f, -0.946f, 0.9913f, 0.1189f, -0.0557f, -0.011f,
            0.035f, -0.072f, -0.95f, 0.9912f, 0.1193f, -0.0559f, -0.01f,
            0.036f, -0.072f, -0.948f, 0.9912f, 0.1195f, -0.056f, -0.0087f,
            0.036f, -0.073f, -0.949f, 0.9912f, 0.1196f, -0.0561f, -0.0076f,
            0.036f, -0.072f, -0.948f, 0.9912f, 0.1198f, -0.0561f, -0.0067f,
            0.036f, -0.073f, -0.948f, 0.9912f, 0.1198f, -0.0561f, -0.0055f,
            0.037f, -0.072f, -0.949f, 0.9912f, 0.1197f, -0.0563f, -0.0042f,
            0.036f, -0.073f, -0.949f, 0.9912f, 0.1198f, -0.0564f, -0.003f,
            0.036f, -0.073f, -0.946f, 0.9912f, 0.1199f, -0.0566f, -0.0017f,
            0.036f, -0.073f, -0.949f, 0.9912f, 0.1195f, -0.0567f, -0.0005f,
            0.036f, -0.073f, -0.949f, 0.9912f, 0.1197f, -0.0569f, 0.0007f,
            0.037f, -0.072f, -0.949f, 0.9912f, 0.1198f, -0.0569f, 0.002f,
            0.036f, -0.073f, -0.948f, 0.9912f, 0.1197f, -0.0569f, 0.0031f,
            0.035f, -0.072f, -0.947f, 0.9911f, 0.1201f, -0.057f, 0.0049f,
            0.031f, -0.076f, -0.948f, 0.9911f, 0.1201f, -0.0575f, 0.0069f,
            0.042f, -0.069f, -0.948f, 0.9911f, 0.1198f, -0.0575f, 0.0104f,
            0.036f, -0.073f, -0.949f, 0.9911f, 0.1197f, -0.0573f, 0.0118f,
            0.04f, -0.068f, -0.948f, 0.9911f, 0.1194f, -0.0567f, 0.0133f,
            0.039f, -0.057f, -0.947f, 0.9911f, 0.1192f, -0.0566f, 0.0149f,
            0.01f, -0.034f, -0.949f, 0.9912f, 0.1181f, -0.0575f, 0.0148f,
            -0.117f, 0.028f, -0.956f, 0.991f, 0.1214f, -0.0541f, -0.0126f,
            0.202f, -0.215f, -0.937f, 0.9908f, 0.123f, -0.0525f, -0.02f,
            0.042f, -0.087f, -0.952f, 0.991f, 0.1221f, -0.0524f, -0.0152f,
            0.069f, -0.05f, -0.95f, 0.9912f, 0.1215f, -0.0529f, -0.0069f,
            0.015f, -0.045f, -0.95f, 0.9913f, 0.1205f, -0.053f, 0.0002f,
            0.034f, -0.071f, -0.949f, 0.9915f, 0.1191f, -0.0531f, 0.003f,
            0.051f, -0.079f, -0.952f, 0.9914f, 0.1192f, -0.0537f, 0.0077f,
            0.029f, -0.074f, -0.947f, 0.9913f, 0.1186f, -0.0549f, 0.0126f,
            0.026f, -0.077f, -0.946f, 0.9913f, 0.119f, -0.0547f, 0.0146f
    };
}
