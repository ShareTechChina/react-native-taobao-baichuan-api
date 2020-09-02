//
//  React_Native_Taobao_Baichuan_Api.m
//  React-Native-Taobao-Baichuan-Api
//
//  Created by Xie, Wang on 10/28/16.
//  Copyright © 2016 Xie, Wang. All rights reserved.
//

#import "React_Native_Taobao_Baichuan_Api.h"
#import <UIKit/UIWebView.h>
#import "AppDelegate.h"
#import <AlibabaAuthSDK/ALBBSDK.h>

#import <AlibabaAuthSDK/ALBBSession.h>
#import <AlibabaAuthSDK/ALBBUser.h>
#import "ALiTradeWebViewController.h"
#import "TaobaoOAuthViewController.h"

@implementation React_Native_Taobao_Baichuan_Api{
    UIViewController *mainViewController;
}

-(id) init{
    if(self == [super init]){
        mainViewController = [UIApplication sharedApplication].delegate.window.rootViewController;
    }
    
    return self;
}

RCT_EXPORT_MODULE(React_Native_Taobao_Baichuan_Api);

RCT_EXPORT_METHOD(jumpByUrl:(NSString *)url callback:(RCTResponseSenderBlock)callback)
{
    //淘客信息
    AlibcTradeTaokeParams *taoKeParams=[[AlibcTradeTaokeParams alloc] init];
    taoKeParams.pid=  @"mm_120032403_0_0";
    //打开方式
    AlibcTradeShowParams* showParam = [[AlibcTradeShowParams alloc] init];
    showParam.openType = AlibcOpenTypeAuto;
    
    showParam.isNeedPush = YES;
    
//    ALiTradeWebViewController* view = [[ALiTradeWebViewController alloc] init];
    TaobaoOAuthViewController* view = [[TaobaoOAuthViewController alloc] init];

    
    NSInteger ret = [[AlibcTradeSDK sharedInstance].tradeService
                     openByUrl: url
                     identity:@"trade"
                     webView: view.myWebView
                     parentController: mainViewController
                     showParams: showParam
                     taoKeParams: taoKeParams
                     trackParam:nil
                     tradeProcessSuccessCallback:^(AlibcTradeResult * __nullable result) {
                         if (result.result == AlibcTradeResultTypePaySuccess) {
                             NSDictionary *res = @{@"result": @"success", @"orders": result.payResult.paySuccessOrders};
                             callback(@[[NSNull null], res]);
                         } else {
                             callback(@[[NSNull null], [NSNull null]]);
                         }
                         
                         NSLog(@"%@", result);
                     }
                     tradeProcessFailedCallback:^(NSError * __nullable error) {
                         callback(@[[NSNull null],[NSNull null]]);
                         
                         NSLog(@"%@", error);
                     }
                     ];
    //返回1,说明h5打开,否则不应该展示页面
    if (ret == 1) {
        UIViewController *appRootVC = [UIApplication sharedApplication].keyWindow.rootViewController;
        [appRootVC presentViewController:view animated:YES completion:nil];
        
        //接受oauth结果
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(urlChange:) name:@"oauth" object:nil];
    }
    
    [[ALBBSDK sharedInstance] setLoginResultHandler:^(ALBBSession *session) {
        if([session isLogin]){//登录成功
            ALBBUser *user ;
            user = [[ALBBSession sharedInstance] getUser];
            NSMutableDictionary *retUser = [NSMutableDictionary dictionaryWithCapacity:3];
            [retUser setObject:user.nick forKey:@"nick"];
            [retUser setObject:user.openId forKey:@"openId"];
            [retUser setObject:user.openSid forKey:@"openSid"];
            
            [self sendEventWithName:@"EventReminder" body:@{@"user": retUser}];
            
            NSLog(@"用户login");
        }else{//已登录变为未登录
            NSLog(@"用户logout");
        }
        [self sendEventWithName:@"backFromTB" body:@"true"];
    }];
    
    return ;
    
}

- (void)urlChange:(NSNotification *)notification{
    [self sendEventWithName:@"oauthEvent" body: notification.object];
}

RCT_EXPORT_METHOD(jump:(NSString *)itemId :(NSString *) orderId type:(NSString *)type callback:(RCTResponseSenderBlock)callback)
{
    
    [self itemDetailPage: itemId: orderId: type: callback];
    
}
- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
-(void)itemDetailPage: (NSString *) item: (NSString *) orderId: (NSString *) type: (RCTResponseSenderBlock)callback{
    
    NSString *itemID = item;
    NSString *orderID = orderId;
    id<AlibcTradePage> page = [AlibcTradePageFactory itemDetailPage: itemID];
    //淘客信息
    AlibcTradeTaokeParams *taoKeParams=[[AlibcTradeTaokeParams alloc] init];
    taoKeParams.pid=  @"mm_120032403_0_0";;
    //打开方式
    AlibcTradeShowParams* showParam = [[AlibcTradeShowParams alloc] init];
    if([type isEqual:@"tmall"] ){
        showParam.openType = AlibcOpenTypeNative;
    } else {
        showParam.openType = AlibcOpenTypeAuto;
    }
    
    showParam.isNeedPush = NO;
    
    ALiTradeWebViewController* view = [[ALiTradeWebViewController alloc] init];

    //打开我的订单页
    id<AlibcTradePage> pageOrder = [AlibcTradePageFactory myOrdersPage:orderID isAllOrder:NO];
    
    NSInteger ret = [[AlibcTradeSDK sharedInstance].tradeService
                     openByBizCode: @"detail"
                     page: [item  isEqual: @""]? pageOrder:page
                     webView: view.webView
                     parentController: view
                     showParams: showParam
                     taoKeParams: taoKeParams
                     trackParam:nil
                     tradeProcessSuccessCallback:^(AlibcTradeResult * __nullable result) {
                         if (result.result == AlibcTradeResultTypePaySuccess) {
                             NSDictionary *res = @{@"result": @"success", @"orders": result.payResult.paySuccessOrders};
                             callback(@[[NSNull null], res]);
                         } else {
                             callback(@[[NSNull null], [NSNull null]]);
                         }
                         
                         NSLog(@"%@", result);
                     }
                     tradeProcessFailedCallback:^(NSError * __nullable error) {
                         callback(@[[NSNull null],[NSNull null]]);
                         
                         NSLog(@"%@", error);
                     }
                     ];
    //返回1,说明h5打开,否则不应该展示页面
    if (ret == 1) {
        //        [self.navigationController pushViewController:view animated:YES];
    }
    
    [[ALBBSDK sharedInstance] setLoginResultHandler:^(ALBBSession *session) {
        if([session isLogin]){//登录成功
            ALBBUser *user ;
            user = [[ALBBSession sharedInstance] getUser];
            NSMutableDictionary *retUser = [NSMutableDictionary dictionaryWithCapacity:3];
            [retUser setObject:user.nick forKey:@"nick"];
            [retUser setObject:user.openId forKey:@"openId"];
            [retUser setObject:user.openSid forKey:@"openSid"];
            
            [self sendEventWithName:@"EventReminder" body:@{@"user": retUser}];
            
            NSLog(@"用户login");
        }else{//已登录变为未登录
            NSLog(@"用户logout");
        }
        [self sendEventWithName:@"backFromTB" body:@"true"];
    }];
    
    return ;
}

- (NSArray<NSString *> *)supportedEvents
{
    return @[@"EventReminder",@"backFromTB", @"oauthEvent"];
}

RCT_EXPORT_METHOD(login:(RCTResponseSenderBlock)callback)
{
    
//    [[ALBBSDK sharedInstance] logout];
   if([[ALBBSession sharedInstance] isLogin]){//登录成功
       ALBBUser *user ;
       user = [[ALBBSession sharedInstance] getUser];
       NSMutableDictionary *retUser = [NSMutableDictionary dictionaryWithCapacity:3];
       [retUser setObject:user.nick forKey:@"nick"];
       [retUser setObject:user.openId forKey:@"openId"];
       [retUser setObject:user.openSid forKey:@"openSid"];
       [retUser setObject:user.topAccessToken forKey:@"topAccessToken"];
       [retUser setObject:user.avatarUrl forKey:@"avatarUrl"];
       callback(@[@{@"user": retUser}]);
   } else {//已登录变为未登录
       [[ALBBSDK sharedInstance] auth:[UIApplication sharedApplication].delegate.window.rootViewController
                      successCallback:^(ALBBSession *session) {
           NSLog(@"登录成功：%@",[session getUser]);
           ALBBUser *user ;
           user = [[ALBBSession sharedInstance] getUser];
           NSMutableDictionary *retUser = [NSMutableDictionary dictionaryWithCapacity:3];
           [retUser setObject:user.nick forKey:@"nick"];
           [retUser setObject:user.openId forKey:@"openId"];
           [retUser setObject:user.openSid forKey:@"openSid"];
           [retUser setObject:user.topAccessToken forKey:@"topAccessToken"];
           [retUser setObject:user.avatarUrl forKey:@"avatarUrl"];
           callback(@[@{@"user": retUser}]);
       } failureCallback:^(ALBBSession *session, NSError *error) {
           callback(@[@{@"user": @""}]);
           NSLog(@"登录失败");
       }];
   }
}


@end
