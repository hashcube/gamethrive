#import "GameThrivePlugin.h"
#import <OneSignal/OneSignal.h>

@interface GameThrivePlugin()

@property (strong, nonatomic) OneSignal *oneSignal;
@property (nonatomic, retain) NSData *deviceToken;
@property (nonatomic) BOOL initDone;
@property (nonatomic, retain) NSMutableDictionary *tags;

@end

@implementation GameThrivePlugin

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

- (void) initializeWithManifest:(NSDictionary *)manifest appDelegate:(TeaLeafAppDelegate *)appDelegate {
    @try {
        //ONLY DURING DEBUG
        //[OneSignal setLogLevel: ONE_S_LL_VERBOSE visualLevel: ONE_S_LL_NONE];

        NSDictionary *ios = [manifest valueForKey:@"ios"];
        NSString *gamethriveAppId = [ios valueForKey:@"gameThriveAppID"];
        NSDictionary *launchOptions = appDelegate.startOptions;

        //initialize here
        self.oneSignal = [[OneSignal alloc] initWithLaunchOptions:launchOptions
                                                            appId: gamethriveAppId
                                               handleNotification: NULL
                                                     autoRegister:YES];

        self.initDone = YES;
        NSLOG(@"{gamethrive} Initialized");

        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    }
    @catch (NSException *exception) {
        NSLog(@"{gamethrive} Failed to initialize with exception: %@", exception);
    }
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
    NSLog(@"{gamethrive} didFailtoRegisterforremotenotifications: %@", error);
}

- (void) didReceiveRemoteNotification:(NSDictionary *)userInfo application:(UIApplication *)app {
    //tracking last launch time
    NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
    [dateFormat setDateFormat: @"yyyy-MM-dd HH:mm:ss zzz"];
    NSString *formattedDateString = [dateFormat stringFromDate:[NSDate date]];
    NSNumber* timestamp = [NSNumber numberWithDouble:[[NSDate date] timeIntervalSince1970] * 1000] ;

    // tracking number of launches because of notification
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSInteger counter = [defaults integerForKey:@"launch_count"];
    counter += 1;

    NSInteger badge_count = [[[userInfo objectForKey:@"aps"] objectForKey:@"badge"] intValue];
    NSInteger saved_badge_count = [defaults integerForKey:@"badge_count"];
    if(saved_badge_count) {
        badge_count += saved_badge_count;
    }

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
                              [[[userInfo objectForKey:@"custom"] objectForKey:@"a"] objectForKey:@"segment_name"]];
    if(segment_name == @"(null)" || segment_name != nil) {
        segment_name = @"unknown";
    }

    NSString* message = [NSString stringWithFormat: @"%@",
                         [[[userInfo objectForKey:@"aps"] objectForKey:@"alert"] objectForKey:@"body"]];
    NSString* title = [NSString stringWithFormat: @"%@",
                         [[[userInfo objectForKey:@"aps"] objectForKey:@"alert"] objectForKey:@"title"]];

    // sending number of received messages from the last time
    NSDictionary* notification_data = [NSDictionary dictionaryWithObjectsAndKeys:
                                       segment_name, @"notification_segment_name",
                                       title, @"notification_title",
                                       message, @"notification_message",
                                       timestamp, @"last_notification_opened_on",
                                       [NSNumber numberWithInteger:counter],
                                            @"notification_opened_count",
                                       timestamp, @"last_notification_received_on",
                                       [NSNumber numberWithInteger:badge_count], @"notification_received_count", nil];

    [[PluginManager get] dispatchJSEvent:[NSDictionary dictionaryWithObjectsAndKeys:
                                          @"gamethriveNotificationReceived", @"name",
                                          [NSString stringWithFormat: @"%@",notification_data], @"notification_data",
                                          NO, @"failed", nil]];
}

- (void) sendUserTags:( NSDictionary *)tags {
    if(self.tags == nil) {
        self.tags = [[NSMutableDictionary alloc] initWithDictionary:tags copyItems:true];
    } else {
        [self.tags addEntriesFromDictionary: tags];
    }
    if(self.initDone && self.deviceToken) {
        [self.oneSignal sendTags: self.tags];
        NSLOG(@"tags: %@", self.tags);
        [self.tags removeAllObjects];
    }
}

@end
