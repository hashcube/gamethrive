#import "OnesignalPlugin.h"
#import <OneSignal/OneSignal.h>

@interface OnesignalPlugin()

@property (strong, nonatomic) OneSignal *oneSignal;
@property (nonatomic, retain) NSData *deviceToken;
@property (nonatomic) BOOL initDone;
@property (nonatomic, retain) NSMutableDictionary *tags;
@property (nonatomic, retain) NSDictionary *notification_data;

@end

@implementation OnesignalPlugin

// The plugin must call super dealloc.
- (void) dealloc {
    [self.deviceToken release];
    [self.tags release];
    [super dealloc];
}

// The plugin must call super init.
- (id) init {
    if(self = [super init]) {
        self.initDone = NO;
        self.deviceToken = nil;
        self.tags = nil;
    }
    return self;
}

- (BOOL) didFinishLaunchingWithOptions:(NSDictionary *)launchOptions application:(UIApplication *)app {
    @try {

        //ONLY DURING DEBUG
        [OneSignal setLogLevel: ONE_S_LL_VERBOSE visualLevel: ONE_S_LL_NONE];

        NSString *onesignalAppId = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"onesignalAppID"];
        NSDictionary *launchOptions = launchOptions;
        //initialize here
        self.oneSignal = [[OneSignal alloc] initWithLaunchOptions:launchOptions
                                                            appId: onesignalAppId
                                               handleNotification: ^(NSString* message, NSDictionary* additionalData, BOOL isActive) {
                                                   NSLog(@"{onesignal}Notification opened:\nMessage: %@", message);
                                                   // Triggered when user opens app from notification receieved
                                                   // Hence notification opened count and receievd count will increment by 1
                                                   //tracking last launch time
                                                   NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
                                                   [dateFormat setDateFormat: @"yyyy-MM-dd HH:mm:ss zzz"];
                                                   NSString *formattedDateString = [dateFormat stringFromDate:[NSDate date]];
                                                   NSNumber* timestamp = [NSNumber numberWithDouble:[[NSDate date] timeIntervalSince1970] * 1000] ;

                                                   // tracking number of launches because of notification
                                                   NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
                                                   NSInteger counter = [defaults integerForKey:@"launch_count"];
                                                   counter += 1;

                                                   NSInteger badge_count = [[additionalData objectForKey:@"badge"] intValue] + 1;
                                                   NSInteger saved_badge_count = [defaults integerForKey:@"badge_count"];
                                                   badge_count += saved_badge_count;

                                                   [defaults setInteger:counter forKey:@"launch_count"];
                                                   [defaults setInteger:badge_count forKey:@"badge_count"];
                                                   [defaults synchronize];

                                                   NSDictionary* dict = [NSDictionary dictionaryWithObjectsAndKeys:
                                                                         [NSNumber numberWithInteger:counter], @"notification_opened_count",
                                                                         [NSNumber numberWithInteger:badge_count], @"notification_received_count",
                                                                         formattedDateString, @"last_notification_received_on",
                                                                         formattedDateString, @"last_notification_opened_on", nil];
                                                   [self.oneSignal sendTags: dict];

                                                   NSString* segment_name = [NSString stringWithFormat: @"%@",
                                                                             [additionalData objectForKey:@"segment_name"]];

                                                   if([segment_name isEqualToString:@"(null)"] || segment_name == nil) {
                                                       segment_name = @"unknown";
                                                   }

                                                   //NSString* message = [NSString stringWithFormat: @"%@", message];
                                                   NSString* title = [NSString stringWithFormat: @"%@",
                                                                      [additionalData objectForKey:@"title"]];

                                                   // Reset counter and badge_count to 1
                                                   counter = 1;
                                                   badge_count = 1;

                                                   // sending number of received messages from the last time
                                                   self.notification_data = [NSDictionary dictionaryWithObjectsAndKeys:
                                                                             segment_name, @"notification_segment_name",
                                                                             title, @"notification_title",
                                                                             message, @"notification_message",
                                                                             timestamp, @"last_notification_opened_on",
                                                                             [NSNumber numberWithInteger:counter],
                                                                             @"notification_opened_count",
                                                                             timestamp, @"last_notification_received_on",
                                                                             [NSNumber numberWithBool:isActive], @"is_active",
                                                                             [NSNumber numberWithInteger:badge_count], @"notification_received_count", nil];

                                                   NSError *error;
                                                   NSData *jsonData = [NSJSONSerialization dataWithJSONObject:self.notification_data
                                                                                                      options:NSJSONWritingPrettyPrinted
                                                                                                        error:&error];

                                                   if (! jsonData) {
                                                       NSLog(@"{onesignal} Got a json error: %@", error);
                                                   } else {
                                                       NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
                                                       [[PluginManager get] dispatchJSEvent:[NSDictionary dictionaryWithObjectsAndKeys:
                                                                                             @"onesignalNotificationOpened", @"name",
                                                                                             jsonString, @"notification_data",
                                                                                             NO, @"failed", nil]];

                                                   }
                                               }
                                            autoRegister:YES];
        NSLog(@"{onesignal} initDone");
        self.initDone = YES;
    }
    @catch (NSException *exception) {
        NSLog(@"{onesignal} Failed to initialize with exception: %@", exception);
    }
    return true;
}

- (void) initializeWithManifest:(NSDictionary *)manifest appDelegate:(TeaLeafAppDelegate *)appDelegate {
}

- (void) didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken application:(UIApplication *)app {
    if(self.initDone && (self.deviceToken == nil)) {
        self.deviceToken = deviceToken;
        if(self.tags) {
            [self.oneSignal sendTags: self.tags];
            [self.tags removeAllObjects];
        }
    }
}

- (void) didFailToRegisterForRemoteNotificationsWithError:(NSError *)error application:(UIApplication *)app {
    NSLog(@"{onesignal} didFailtoRegisterforremotenotifications: %@", error);
}

- (void) didReceiveRemoteNotification:(NSDictionary *)userInfo application:(UIApplication *)app {
}

- (void) sendUserTags:( NSDictionary *)tags {
    if(self.tags == nil) {
        self.tags = [[NSMutableDictionary alloc] initWithDictionary:tags copyItems:true];
    } else {
        [self.tags addEntriesFromDictionary: tags];
    }
    if(self.initDone && self.deviceToken) {
        [self.oneSignal sendTags: self.tags];
        NSLog(@"tags: %@", self.tags);
        [self.tags removeAllObjects];
    }
}

- (void) getNotificationData:( NSDictionary *)tags withRequestId:(NSNumber *)requestId  {
    [[PluginManager get] dispatchJSResponse:self.notification_data withError:nil andRequestId:requestId];
    self.notification_data = nil;
}

@end
